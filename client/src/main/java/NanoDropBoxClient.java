import common.Comands;
import common.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class NanoDropBoxClient {

    private static final int PORT = 12256;
    private static final String HOST = "localhost";
    public String directoryForControl;
    public boolean restored = false;
    private String validate = "FALSE";
    private String login;
    private NanoDropBoxClient nanoDBClient = this;
    private String baseFilePathForStore;
    private Watchers ws;
    private List<String> allFiles;

    public void start() {

        Scanner scanner;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(
                                            ClassResolvers.weakCachingConcurrentResolver(null)),
                                    new ClientDecoder(nanoDBClient));
                        }
                    });
            ClientApp.LOGGER_CLIENT.log(Level.valueOf("Info"), "From NanoDropBoxClient - Client started");
            ChannelFuture future = b.connect(HOST, PORT).sync();

            scanner = new Scanner(System.in);
            System.out.println("Go through authorization.");  // Диалог авторизация
            while (validate.equals("FALSE")) {
                validate = "NULL";
                System.out.println("Enter login: ");
                login = scanner.nextLine();
                System.out.println("Enter pass: ");
                String pass = scanner.nextLine();
                FileUploadFile ful = new FileUploadFile(login, Comands.LOGIN);
                ful.setPass(pass);
                future.channel().writeAndFlush(ful).sync();
                while (validate.equals("NULL") && future.channel().isOpen()) Thread.sleep(1000);
            }
            // Диалог восстановления файлов с сервера
            System.out.println("If you want to restore saved files input 1 : ");
            String s = scanner.nextLine();
            if (s.equals("1")) {
                while (!restored) {
                    future.channel().writeAndFlush(new FileUploadFile(login)).sync();
                    Thread.sleep(3000);
                }
                System.out.println("All restored.");
                ClientApp.LOGGER_CLIENT.log(Level.valueOf("Info"), "From NanoDropBoxClient - All restored.");
            }
            // Запрос адреса директории для синхронизации
            do {
                System.out.println("Enter Directory for sync: ");
                directoryForControl = scanner.nextLine();
            } while (!new File(directoryForControl).isDirectory());
            // Создание на сервере копии нужной директории
            allFiles = Arrays.stream(getFileList(directoryForControl)).toList();
            allFiles = initPaths(allFiles);
            allFiles.add(directoryForControl);
            future.channel().writeAndFlush(new FileUploadFile(allFiles, login, Comands.INIT)).sync();
            initDirectory(future, allFiles);
            while (baseFilePathForStore == null) Thread.sleep(1000);
            //  Создание системы отслеживания изменения файлов
            ws = new Watchers(future, allFiles, this, baseFilePathForStore);
            ws.run();
            // Диалог завершения работы
            while (true) {
                System.out.println("Enter comand (1 - All delete and stop, 2 - Stop): ");
                String comand = scanner.nextLine();
                switch (comand) {
                    case ("1") -> {
                        ws.onDelete(baseFilePathForStore + login);
                        System.out.println("All restored.");
                        ClientApp.LOGGER_CLIENT.log(Level.valueOf("Info"), "From NanoDropBoxClient - All restored.");
                        allStop(future);
                    }
                    case ("2") -> {
                        allStop(future);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            ClientApp.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From NanoDropBoxClient - " + e);

        } finally {
            group.shutdownGracefully();
        }
    }

    private void allStop(ChannelFuture future) {
        future.channel().writeAndFlush(new FileUploadFile(this.login, Comands.STOP));
        System.out.println("All Stopped.");
        ClientApp.LOGGER_CLIENT.log(Level.valueOf("Info"), "From NanoDropBoxClient - All Stopped.");
        System.exit(0);
    }

    public void setValidate(String validate) {
        this.validate = validate;
    }
    // Инициализация директории на сервере
    void initDirectory(ChannelFuture f, List<String> allFiles) throws InterruptedException {
        FileUploadFile ful;
        for (String fileByFile : allFiles) {
                ful = new FileUploadFile(new File(fileByFile), login, Comands.WRITE);
                f.channel().writeAndFlush(ful).sync();
        }
    }
    // Создание массива путей файлов и папок вложенных в текущий каталог
    List<String> initPaths(List<String> allFiles) throws InterruptedException {
        List<String> returnedAllFilesInner = new ArrayList<>(allFiles);
        for (String fileByFile : allFiles) {
            if (new File(fileByFile).isDirectory()) {
                returnedAllFilesInner.addAll(initPaths(Arrays.asList(getFileList(fileByFile))));
            }
        }
        return returnedAllFilesInner;
    }
    // Создание списка файлов в текущей папке
    public String[] getFileList(String dirPath) {
        File file = new File(dirPath);
        String[] filesFullPath  = file.list();
        for (int i = 0; i < filesFullPath.length; i++) {
            filesFullPath[i] = dirPath + "/" + filesFullPath[i];
        }
        return filesFullPath;
    }

    public String getLogin() {
        return login;
    }

    public void setAllFiles(List<String> allFiles) {
        this.allFiles = allFiles;
    }
    // Получение клиентом адреса хранения файлов на сервере
    public void setBaseFilePathForStore(String baseFilePathForStore) {
        this.baseFilePathForStore = baseFilePathForStore;
    }

}
