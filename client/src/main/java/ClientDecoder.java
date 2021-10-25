import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ClientDecoder extends SimpleChannelInboundHandler<FileUploadFile> {

    private  NanoDropBoxClient nanoDBClient;
    private static final int CHUNK_LENGTH = 2000000;

    public ClientDecoder(NanoDropBoxClient nanoDBClient) {
        this.nanoDBClient = nanoDBClient;
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {

        switch (msg.getComand()) {  // Перебор команд от сервера.
            case LOGIN -> {  // Авторизация со стороны клиента
                if (msg.getName().equals(nanoDBClient.getLogin())) {
                    nanoDBClient.setBaseFilePathForStore(msg.getFileName());
                    nanoDBClient.setValidate(msg.getName());
                } else nanoDBClient.setValidate("FALSE");
            }
            case RESTORE -> { // Действие по команде восстановления с сервера (файлы)
                String fileName = msg.getFileName().substring((msg.getName()).length());
                if (msg.getFile().isDirectory()) new File(fileName).mkdirs();
                else restoreFile(msg.getFile(), fileName);
            }
            case RESTORE_PATHS -> { // Действие по команде восстановления с сервера (пути)
                if (msg.getAllPaths() != null) {
                    nanoDBClient.setAllFiles(msg.getAllPaths());
                    nanoDBClient.setBaseFilePathForStore(msg.getName());
                }
                nanoDBClient.restored = true;
            }

        }

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

    private void restoreFile(File file, String destinationPath) throws IOException {
        // Метод восстановления файла
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

}
