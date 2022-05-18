package cn.edu.djtu.db;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author zzx
 * @date 2022/4/21
 */
public class Queue extends SyncPrimitive{
    String name;

    Queue(String address, String name) {
        super(address);
        this.root = name;
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
    }

    boolean produce(int i) throws InterruptedException, KeeperException {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);

        zk.create(root + "/element", b.array(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        return true;
    }

    int consume() throws InterruptedException, KeeperException {
        int returnValue;
        while (true) {
            synchronized(mutex) {
                List<String> children = zk.getChildren(root, true);
                if (children.size() == 0) {
                    mutex.wait();
                    System.out.println("Need to wait for elements!");
                } else {
                    String minNode = children.get(0);
                    int min = Integer.parseInt(minNode.substring(7));
                    for (String c : children) {
                        int prefix = Integer.parseInt(c.substring(7));
                        // find the min element
                        if (prefix < min) {
                            min = prefix;
                            minNode = c;
                        }
                    }
                    byte[] data = zk.getData(root + "/" + minNode, false, null);
                    zk.delete(root + "/" + minNode, 0);
                    returnValue = ByteBuffer.wrap(data).getInt();
                    return returnValue;
                }
            }
        }
    }

}
