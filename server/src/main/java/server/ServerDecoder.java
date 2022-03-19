package server;

import auth.AuthService;
import auth.DatabaseAuthService;
import auth.User;
import common.Comands;
import common.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ServerDecoder extends SimpleChannelInboundHandler<FileUploadFile> {
    // Корневной адрес хранения файлов и папок
    private final String filePathForStore =
            "/home/andrey/IntellijWorkPlace/NanoDropBox/server/src/main/resources/Store/";
    // Размер фрагмента файла при записи
    private static final int CHUNK_LENGTH = 2000000;
    private AuthService databaseAuthService;
    private static final String STR_SEPARATOR = "__,__";

    public ServerDecoder() {
        databaseAuthService = new DatabaseAuthService();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FileUploadFile msg) throws Exception {
        if (msg.getComand() != null) {
            String pathForMove;
            switch (msg.getComand()) {  // Перебор команд от клиента
                case LOGIN ->  {  // Авторизация
                    if (msg.getPass() != null) {
                        User user = new User(msg.getName(), msg.getPass());
                        String verify = databaseAuthService.getUsernameByLoginAndPassword(msg.getName(), msg.getPass());
                        if (!verify.equals("FALSE")) {
                            if (databaseAuthService.isUserOnline(user)) {
                                verify = "FALSE";
                            } else {
                                databaseAuthService.addUser(user);
                                msg.setFileName(filePathForStore);
                            }
                        }
                        msg.setName(verify);
                        ctx.writeAndFlush(msg);
                    }
                }
                case DELETE ->  { // Удаление файла, папки
                    pathForMove = msg.getFileName().substring(filePathForStore.length() + msg.getName().length());
                    String allPaths = databaseAuthService.getAllPathsByLogin(msg.getName());
                    List<String> allPathsList = new java.util.ArrayList<>(stringToAllFiles(allPaths));
                    allPathsList.remove(pathForMove);
                    allPaths = allFilesToString(allPathsList);
                    databaseAuthService.setAllPathsByLogin(msg.getName(), allPaths);
                    FileUploadFile mgsNull = new FileUploadFile(msg);
                    File file = mgsNull.getFile();
                    file.delete();
                }
                case WRITE ->  { // Запись файла, папки
                    String allPaths = databaseAuthService.getAllPathsByLogin(msg.getName());
                    pathForMove = msg.getFileName();
                    List<String> allPathsList = new java.util.ArrayList<>(stringToAllFiles(allPaths));
                    if (!allPathsList.contains(pathForMove)) allPathsList.add(pathForMove);
                    allPaths = allFilesToString(allPathsList);
                    databaseAuthService.setAllPathsByLogin(msg.getName(), allPaths);
                    writeMessage(msg);
                }
                case STOP ->  { // Действия при остановке клиента
                    databaseAuthService.delUser(msg.getName());
                    ctx.close();
                }
                case INIT ->  { // Инициализация путей
                    String allPaths = allFilesToString(msg.getAllPaths());
                    databaseAuthService.setAllPathsByLogin(msg.getName(), allPaths);
                }
            }
        }
        // Передача данных для восстановления с сервера на скиента
        if (msg.getComand() == null && msg.getName() != null ) {
            String allPaths = databaseAuthService.getAllPathsByLogin(msg.getName());
            if (allPaths != null && !allPaths.equals("")) {
                List<String> allFiles = stringToAllFiles(allPaths);
                for (String file : allFiles) {
                    String filePathForStoreLog = filePathForStore + msg.getName();
                    ctx.writeAndFlush(new FileUploadFile(new File(filePathForStoreLog + file), filePathForStoreLog, Comands.RESTORE));
                }
                ctx.writeAndFlush(new FileUploadFile(allFiles, filePathForStore, Comands.RESTORE_PATHS));
            } else ctx.writeAndFlush(new FileUploadFile(msg.getName(), Comands.RESTORE_PATHS));
        }
    }

    private String allFilesToString(List<String> allPaths) {
        StringBuilder str = new StringBuilder();
        String[] array = allPaths.toArray(new String[0]);
        for (int i = 0; i < array.length; i++) {
            str.append(array[i]);
            if (i < array.length - 1) {
                str.append(STR_SEPARATOR);
            }
        }
        return str.toString();
    }

    private List<String> stringToAllFiles(String allPaths) {
        return List.of(allPaths.split(STR_SEPARATOR));
    }
    // Метод записи файла или директории
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
    // Непосредственная запись файла на сервер
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
        ServerApp.LOGGER_SERVER.log(Level.valueOf("Info"), "From ServerDecoder - New channel is active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ServerApp.LOGGER_SERVER.log(Level.valueOf("Info"), "From ServerDecoder - Client inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ServerApp.LOGGER_SERVER.log(Level.valueOf("Warn"), "From ServerDecoder - " + cause.toString());
    }
}
