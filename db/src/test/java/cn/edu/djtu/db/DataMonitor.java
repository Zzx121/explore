package cn.edu.djtu.db;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;

public class DataMonitor implements AsyncCallback.StatCallback, Watcher {
    ZooKeeper zk;
    String znode;
    Watcher chainedWatcher;
    boolean dead;
    DataMonitorListener listener;
    byte prevData[];

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;
        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        zk.exists(znode, true, this, null);
    }
    @Override
    public void processResult(int i, String s, Object o, Stat stat) {
        boolean exists = false;
        switch (i) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.NoAuth:
            case KeeperException.Code.SessionExpired:
                dead = true;
                listener.closing(i);
                return;
            default:
                zk.exists(znode, true, this, null);
        }

        byte[] b = null;
        if (exists) {
            try {
                b = zk.getData(znode, true, stat);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        String path = watchedEvent.getPath();
        Event.EventType type = watchedEvent.getType();
        // state has changed
        if (Event.EventType.None.equals(type)) {
            Event.KeeperState state = watchedEvent.getState();
            switch (state) {
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.closing(KeeperException.Code.SESSIONEXPIRED.intValue());
                    break;
            }
        } else {
            if (znode.equals(path)) {
                zk.exists(path, true, this, null);
            }
        }

        if (chainedWatcher != null) {
            chainedWatcher.process(watchedEvent);
        }

    }
}
