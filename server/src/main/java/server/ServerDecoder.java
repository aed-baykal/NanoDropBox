package server;

import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerDecoder extends SimpleChannelInboundHandler<FileUploadFile> {

    private final String filePathForStore =
            "/home/andrey/IntellijWorkPlace/NanoDropBox/server/src/main/resources/Store";
    private static final int CHUNKE_LENGTH = 2000000;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {
        System.out.println("Receive new message from Client.");
        String filePath = msg.getFileName();
        File file = msg.getFile();
        prepareFilePlace(filePath, file);
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(filePathForStore + filePath);
        writeFile(file, fileInputStream, fileOutputStream);
        ctx.close();

    }

    private void prepareFilePlace(String filePath, File file) {
        String destinationPath = filePathForStore + filePath;
        destinationPath = destinationPath
                .substring(0, destinationPath.length() - file.getName().length());
        new File(destinationPath).mkdirs();
    }

    private void writeFile(File file, FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        long lengthOfFile = file.length();
        int repeat = 0;
        for (int i = 0; i < lengthOfFile / CHUNKE_LENGTH; i++) {
            byte[] fileContent = new byte[CHUNKE_LENGTH];
            fileInputStream.read(fileContent);
            fileOutputStream.write(fileContent);
            repeat++;
        }
        byte[] fileContent = new byte[(int)(lengthOfFile - CHUNKE_LENGTH *repeat)];
        fileInputStream.read(fileContent);
        fileOutputStream.write(fileContent);
        fileInputStream.close();
        fileOutputStream.close();
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
