import io.netty.channel.ChannelFuture;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watchers implements Runnable{

    private Map<Path, Set<Path>> multiMap;
    private Map<WatchKey, Path> keys;
    private WatchService ws;
    private boolean shutdown;
    private long lastModified;
    private Path lastPath;
    ChannelFuture future;

    public Watchers(ChannelFuture future, String[] filesNames) throws IOException{
        this.future = future;
        initMultiMap(filesNames);
        keys = new HashMap<>();
        ws = FileSystems.getDefault().newWatchService();

        for(Path dirName : multiMap.keySet()){
            WatchKey key = dirName.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            keys.put(key, dirName);
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
                        Path fileName = (Path) event.context();
                        Path dirName = keys.get(key);
                        if (multiMap.get(dirName).contains(fileName)) {
                            Path p = Paths.get(dirName.toString(), fileName.toString());
                            long m = p.toFile().lastModified();
                            if (!p.equals(lastPath) || m != lastModified) {
                                System.out.println("Was changed");
                            }
                            lastModified = m;
                            lastPath = p;
                        }
                    } else if (event.kind() == ENTRY_CREATE) {
                        System.out.println("Was created");
                    } else if (event.kind() == ENTRY_DELETE) {
                        System.out.println("Was deleted");
                    }
                }
                key.reset();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void initMultiMap(String[] filesNames){
        multiMap = new HashMap<>();
        for (String filesName : filesNames) {
            Path path = Paths.get(filesName);
            Path fileName = path.getFileName();
            Path dirName = path.getParent();

            if (multiMap.containsKey(dirName)) {
                multiMap.get(dirName).add(fileName);
            } else {
                Set<Path> set = new HashSet<>();
                set.add(fileName);
                multiMap.put(dirName, set);
            }
        }
    }

}
