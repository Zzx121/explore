package cn.edu.djtu.db;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * @author zzx
 * @date 2022/4/20
 */
public class SyncPrimitive implements Watcher {
    static ZooKeeper zk = null;
    static Integer mutex;

    String root;

    SyncPrimitive(String address) {
        if(zk == null){
            try {
                System.out.println("Starting ZK:");
                zk = new ZooKeeper(address, 30000, this);
                mutex = -1;
                System.out.println("Finished starting ZK: " + zk);
            } catch (IOException e) {
                System.out.println(e);
                zk = null;
            }
        }
    }

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notify();
        }
    }
}
