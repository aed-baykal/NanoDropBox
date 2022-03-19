package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.logging.log4j.Level;

public class NanoDropBoxServer {
    private static final int PORT = 12256;
    private final NioEventLoopGroup bossGroup, workerGroup;
    private final ServerBootstrap server;

    public NanoDropBoxServer() {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        this.server = new ServerBootstrap();
    }

    public void start() {
        try {
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(Integer.MAX_VALUE,
                                            ClassResolvers.weakCachingConcurrentResolver(null)),
                                    new ServerDecoder());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Info"), "From NanoDropBoxServer - Server started");
            ChannelFuture sync = server.bind(PORT).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From NanoDropBoxServer - " + e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
