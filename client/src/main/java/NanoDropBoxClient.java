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
import java.util.Scanner;

public class NanoDropBoxClient {

    private static final int PORT = 12256;
    private static final String HOST = "localhost";

    public void start() {
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
            System.out.println("NDBC TEST");

            System.out.println("Enter Directory for sync: ");
            Scanner scanner = new Scanner(System.in);
            String directoryForControl = scanner.nextLine();
            String[] allFiles = getFileList(directoryForControl);
            initDirectory(future, allFiles);
            new Watchers(future, allFiles).run();

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

    private void initDirectory(ChannelFuture f, String[] allFiles) throws InterruptedException {
        for (String fileByFile : allFiles) {
            FileUploadFile ful = new FileUploadFile(new File(fileByFile));
            f.channel().writeAndFlush(ful).sync();
        }
    }

    public String[] getFileList(String dirPath) {
        File file = new File(dirPath);
        String[] filesFullPath  = file.list();
        for (int i = 0; i<filesFullPath.length; i++) {
            filesFullPath[i] = dirPath + "/" + filesFullPath[i];
        }
        return filesFullPath;
    }

}
