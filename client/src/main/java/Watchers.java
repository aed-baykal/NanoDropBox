import common.Comands;
import common.FileUploadFile;
import io.netty.channel.ChannelFuture;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watchers implements Runnable{

    private final Map<WatchKey, Path> keys;
    private final WatchService ws;
    private boolean shutdown;
    ChannelFuture future;
    private final String filePathForStore;
    private NanoDropBoxClient nDBClient;

    public Watchers(ChannelFuture future, List<String> filesNames, NanoDropBoxClient nDBClient, String baseFilePathForStore) throws IOException{
        this.nDBClient = nDBClient;
        this.future = future;
        // Корневой путь хранения файлов на сервере
        this.filePathForStore = baseFilePathForStore + nDBClient.getLogin();
        keys = new HashMap<>();
        ws = FileSystems.getDefault().newWatchService();
        // Создание наблюдателей
        for (String fileByFile : filesNames) {
            if (new File(fileByFile).isDirectory()) {
                WatchKey key = Paths.get(fileByFile).register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                keys.put(key, Paths.get(fileByFile));
            }
        }

    }

    @Override
    public void run() {
        Thread t = new Thread(() -> {
            WatchKey key = null;
            while (!shutdown) {
                try {
                    key = ws.take();
                } catch (InterruptedException e) {
                    ClientApp.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From Watchers - " + e);
                    shutdown = true;
                }
                // Реагирование на изменение, создание, удаление
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == ENTRY_DELETE) onDelete(key, event);
                    else if (event.kind() == ENTRY_CREATE) onCreate(key, event);
                    else if (event.kind() == ENTRY_MODIFY) onCreate(key, event);
                }
                key.reset();
            }
        });
        t.setDaemon(true);
        t.start();
    }
    // Действие на удаление
    private void onDelete(WatchKey key, WatchEvent<?> event) {
        String dirName = filePathForStore + keys.get(key);
        String filePath = dirName + "/" + event.context();
        if (new File(filePath).isDirectory()) {
            while (Objects.requireNonNull(new File(filePath).listFiles()).length > 0) deleteDir(filePath);
        }
        future.channel().writeAndFlush(new FileUploadFile(new File(filePath), nDBClient.getLogin(), Comands.DELETE));
    }
    // Действие на удаление
    void onDelete(String dirName) {
        if (new File(dirName).isDirectory()) {
            while (Objects.requireNonNull(new File(dirName).listFiles()).length > 0) deleteDir(dirName);
        }
        future.channel().writeAndFlush(new FileUploadFile(new File(dirName), nDBClient.getLogin(), Comands.DELETE));
    }
    // Действие на создание
    private void onCreate(WatchKey key, WatchEvent<?> event) {
        String dirName = keys.get(key).toString();
        String filePath = dirName + "/" + event.context();
        if (new File(filePath).isDirectory()) {
            List<String> allFiles = Arrays.stream(nDBClient.getFileList(filePath)).toList();
            try {
                allFiles = nDBClient.initPaths(allFiles);
                allFiles.add(filePath);
                nDBClient.initDirectory(future, allFiles);
            } catch (InterruptedException e) {
                ClientApp.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From Watchers - " + e);
            }
        }
        createFile(key, event);
    }

    private void createFile(WatchKey key, WatchEvent event) {
        FileUploadFile ful;
        String filePath = keys.get(key) + "/" + event.context();
        ful = new FileUploadFile(new File(filePath), nDBClient.getLogin(), Comands.WRITE);
        try {
            future.channel().writeAndFlush(ful).sync();
        } catch (InterruptedException e) {
            ClientApp.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From Watchers - " + e);
        }
    }

    private void deleteDir(String dirName) {
        if (Objects.requireNonNull(new File(dirName).list()).length > 0) {
            List<String> allFiles =
                    Arrays.stream(nDBClient.getFileList(dirName)).toList();
            try {
                allFiles = nDBClient.initPaths(allFiles);
            } catch (InterruptedException e) {
                ClientApp.LOGGER_CLIENT.log(Level.valueOf("Warn"), "From Watchers - " + e);
            }
            for (String fileByFile : allFiles) {
                future.channel().writeAndFlush(new FileUploadFile(new File(fileByFile), nDBClient.getLogin(), Comands.DELETE));
            }
        }
    }

}
