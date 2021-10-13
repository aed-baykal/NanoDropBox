import common.FileUploadFile;
import io.netty.channel.ChannelFuture;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watchers implements Runnable{


    private Map<WatchKey, Path> keys;
    private WatchService ws;
    private boolean shutdown;
    private long lastModified;
    private Path lastPath;
    ChannelFuture future;
    private final String filePathForStore;
    private NanoDropBoxClient nDBClient;

    public Watchers(ChannelFuture future, List<String> filesNames, NanoDropBoxClient nDBClient) throws IOException{
        this.future = future;
        this.nDBClient = nDBClient;
        this.filePathForStore =
                "/home/andrey/IntellijWorkPlace/NanoDropBox/server/src/main/resources/Store/"
                        + nDBClient.getName();
        keys = new HashMap<>();
        ws = FileSystems.getDefault().newWatchService();

        for (String fileByFile : filesNames) {
            if (new File(fileByFile).isDirectory()) {
                WatchKey key = Paths.get(fileByFile).register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                keys.put(key, Paths.get(fileByFile));
            }
        }

        shutdown = false;
        lastModified = 0L;
        lastPath = null;
    }

    @Override
    public void run() {
        Thread t = new Thread(() -> {
            WatchKey key = null;
            while (!shutdown) {
                try {
                    key = ws.take();
                } catch (InterruptedException e) {
                    shutdown = true;
                }

                for (WatchEvent event : key.pollEvents()) {
                    if (event.kind() == ENTRY_MODIFY) {
//                        createFile(key, event);
                        System.out.println("Was changed");
                    } else if (event.kind() == ENTRY_CREATE) {
                        String dirName = keys.get(key).toString();
                        String filePath = dirName + "/" + event.context();
                        if (new File(filePath).isDirectory()) {
                            List<String> allFiles = Arrays.stream(nDBClient.getFileList(filePath)).toList();
                            try {
                                allFiles = nDBClient.initPaths(allFiles);
                                allFiles.add(filePath);
                                nDBClient.initDirectory(future, allFiles);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        createFile(key, event);
                        System.out.println("Was created");
                    } else if (event.kind() == ENTRY_DELETE) {
                        String dirName = filePathForStore + "/" + keys.get(key);
                        String filePath = dirName + "/" + event.context();
                        System.out.println(keys.get(key).toString() + "/" + event.context());
                        deleteDir(dirName, filePath);
                        deleteDir(dirName, filePath);
                        File file = new File(filePath);
                        System.out.println(filePath);
                        file.delete();
                        System.out.println("Was deleted");
                    }

                }
                key.reset();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void createFile(WatchKey key, WatchEvent event) {
        FileUploadFile ful;
        String filePath = keys.get(key) + "/" + event.context();
        ful = new FileUploadFile(new File(filePath), nDBClient.getName());
        try {
            future.channel().writeAndFlush(ful).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteDir(String dirName, String filePath) {
        if (new File(filePath).isDirectory()) {
            System.out.println("step1");
            if (! (new File(filePath).list() == null)) {
                List<String> allFiles =
                        Arrays.stream(nDBClient.getFileList(filePath)).toList();
                try {
                    allFiles = nDBClient.initPaths(allFiles);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (String fileByFile : allFiles) {
                    new File(fileByFile).delete();
                    System.out.println(fileByFile);
                }
                new File(dirName).delete();
                System.out.println("Was deleted_true");
            }
        }
    }

}
