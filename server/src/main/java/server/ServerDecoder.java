package server;

import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ServerDecoder extends SimpleChannelInboundHandler<FileUploadFile> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {
        System.out.println("Receive new message from Client.");
        String file_path = "/home/andrey/IntellijWorkPlace/NanoDropBox/client/src/main/java/test1.txt";
        File file = msg.getFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(file_path);
        byte[] fileContent = new byte[(int) file.length()];
        fileInputStream.read(fileContent);
        fileInputStream.close();
        fileOutputStream.write(fileContent);
        fileOutputStream.close();
        ctx.close();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("New channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
