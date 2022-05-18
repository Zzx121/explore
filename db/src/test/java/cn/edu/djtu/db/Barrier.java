package cn.edu.djtu.db;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * @author zzx
 * @date 2022/4/21
 */
public class Barrier extends SyncPrimitive{
    int size;
    String name;
    Barrier(String address) {
        super(address);
    }

    Barrier(String address, String root, int size) {
        super(address);
        this.root = root;
        this.size = size;
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (KeeperException | InterruptedException e) {
                System.out.println("Keeper exception when instantiating queue: " + e);
                e.printStackTrace();
            }
        }

        try {
            name = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            System.out.println(e);
        }
    }

    boolean enter() throws InterruptedException, KeeperException {
        zk.create(root + "/" + name, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        while (true) {
            synchronized (mutex) {
                List<String> children = zk.getChildren(root, true);
                // here is the key point of the barrier, when the mutex not full, just wait for others
                if (children.size() < size) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    boolean leave() throws InterruptedException, KeeperException {
        zk.delete(root + "/" + name, 0);
        while (true) {
            synchronized (mutex) {
                List<String> children = zk.getChildren(root, true);
                if (children.size() > 0) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }
}
