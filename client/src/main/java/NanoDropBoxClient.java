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
import java.nio.file.Path;
import java.util.*;

public class NanoDropBoxClient {

    private static final int PORT = 12256;
    private static final String HOST = "localhost";
    private Map<Path, Set<Path>> multiMap;
    public String directoryForControl;
    private String name = "first";

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
                                    new ClientDecoder());
                        }
                    });
            System.out.println("Client started");
            ChannelFuture future = b.connect(HOST, PORT).sync();

            do {
                System.out.println("Enter Directory for sync: ");
                scanner = new Scanner(System.in);
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

            boolean notStopped = true;
            while (notStopped) {
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
                        notStopped = false;
                        break;
                    }
                }
            }
            future.channel().closeFuture().sync();
            System.out.println("NDBC end");

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    void initDirectory(ChannelFuture f, List<String> allFiles) throws InterruptedException {
        FileUploadFile ful;
        for (String fileByFile : allFiles) {
                ful = new FileUploadFile(new File(fileByFile), name);
                f.channel().writeAndFlush(ful).sync();
                System.out.println(fileByFile);
        }
//        ful = new FileUploadFile();
//        ful.setComand("CLOSE");
//        f.channel().writeAndFlush(ful).sync();
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

    public String getName() {
        return name;
    }
}
