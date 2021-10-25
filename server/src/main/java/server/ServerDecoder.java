package server;

import auth.AuthService;
import auth.DatabaseAuthService;
import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ServerDecoder extends SimpleChannelInboundHandler<FileUploadFile> {

    private final String filePathForStore =
            "/home/andrey/IntellijWorkPlace/NanoDropBox/server/src/main/resources/Store/";
    private static final int CHUNK_LENGTH = 2000000;
    private AuthService databaseAuthService;

    public ServerDecoder() {
        databaseAuthService = new DatabaseAuthService();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {
        System.out.println("Receive new message from Client.");
        if (msg.getFileName().length() > 0) {
            if (msg.getFileName().equals(msg.getName() + msg.getComand())) {
                String verify = databaseAuthService.getUsernameByLoginAndPassword(msg.getName(), msg.getComand());
                msg.setComand(verify);
                ctx.writeAndFlush(msg);
            } else writeMessage(msg);
        } else {
            if (msg.getComand().equals("CLOSE")) ctx.close();
        }
    }

    private void writeMessage(FileUploadFile msg) throws IOException {
        String filePath = msg.getFileName();
        File file = msg.getFile();
        String destinationPath = filePathForStore + msg.getName() + filePath;
        prepareFilePlace(destinationPath, file);
        if (!file.isDirectory()) writeFile(file, destinationPath);
        else new File(destinationPath).mkdirs();
    }

    private void prepareFilePlace(String dirPath, File file) {

            dirPath = dirPath
                    .substring(0, dirPath.length() - file.getName().length());
        if (!new File(dirPath).exists()) new File(dirPath).mkdirs();

    }

    private void writeFile(File file, String destinationPath) throws IOException {

        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(destinationPath);
        long lengthOfFile = file.length();
        int repeat = 0;
        if (lengthOfFile/ CHUNK_LENGTH >= 1) {
            for (int i = 0; i < lengthOfFile / CHUNK_LENGTH; i++) {
                byte[] fileContent = new byte[CHUNK_LENGTH];
                fileInputStream.read(fileContent);
                fileOutputStream.write(fileContent);
                repeat++;
            }
        }
        byte[] fileContent = new byte[(int) (lengthOfFile - CHUNK_LENGTH * repeat)];
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
        System.out.println("Client inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
