import common.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

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
            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("NDBC TEST");
            FileUploadFile ful = new FileUploadFile(
                    new File("/home/andrey/IntellijWorkPlace/NanoDropBox/client/src/main/java/test.txt"));
            f.channel().writeAndFlush(ful).sync();
            f.channel().closeFuture().sync();
            System.out.println("NDBC end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
