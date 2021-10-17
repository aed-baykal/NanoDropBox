import common.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

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
    private String validate = "FALSE";
    private String login;
    private final NanoDropBoxClient nanoDBClient = this;

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
            System.out.println("Client started");
            ChannelFuture future = b.connect(HOST, PORT).sync();

            scanner = new Scanner(System.in);
            while (validate.equals("FALSE")) {
                validate = "NULL";
                System.out.println("login: ");
                login = scanner.nextLine();
                System.out.println("pass: ");
                String pass = scanner.nextLine();
                future.channel().writeAndFlush(new FileUploadFile(login, pass)).sync();
                while (validate.equals("NULL") && future.channel().isOpen()) Thread.sleep(1000);
            }

            do {
                System.out.println("Enter Directory for sync: ");
                directoryForControl = scanner.nextLine();
            } while (!new File(directoryForControl).isDirectory());

            List<String> allFiles = Arrays.stream(getFileList(directoryForControl)).toList();
            allFiles = initPaths(allFiles);
            allFiles.add(directoryForControl);
            for (String allFile : allFiles) {
                System.out.println(allFile);
            }
            initDirectory(future, allFiles);
            new Watchers(future, allFiles, this).run();

            while (true) {
                System.out.println("Enter comand: ");
                int comand = scanner.nextInt();
                switch (comand) {
                    case (1): {
                        System.out.println("All deleted");
                        break;
                    }
                    case (2): {
                        System.out.println("All restored");
                        break;
                    }
                    case (3): {
                        System.out.println("All synchronized");
                    }
                    case (4): {
                        System.out.println("All Stopped");
                        System.exit(0);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void setValidate(String validate) {
        this.validate = validate;
    }

    void initDirectory(ChannelFuture f, List<String> allFiles) throws InterruptedException {
        FileUploadFile ful;
        for (String fileByFile : allFiles) {
                ful = new FileUploadFile(new File(fileByFile), login);
                f.channel().writeAndFlush(ful).sync();
        }
    }

    List<String> initPaths(List<String> allFiles) throws InterruptedException {
        List<String> returnedAllFilesInner = new ArrayList<>(allFiles);
        for (String fileByFile : allFiles) {
            if (new File(fileByFile).isDirectory()) {
                returnedAllFilesInner.addAll(initPaths(Arrays.asList(getFileList(fileByFile))));
            }
        }
        return returnedAllFilesInner;
    }

    public String[] getFileList(String dirPath) {
        File file = new File(dirPath);
        String[] filesFullPath  = file.list();
        for (int i = 0; i<filesFullPath.length; i++) {
            filesFullPath[i] = dirPath + "/" + filesFullPath[i];
        }
        return filesFullPath;
    }

    public String getLogin() {
        return login;
    }
}
