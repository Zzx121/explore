package cn.edu.djtu.db;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.*;

/**
 * Response for connection and fetch the data associate with the znode and starts the executable
 */
public class ZookeeperExecutor implements Watcher, Runnable, DataMonitorListener {
    String znode;
    DataMonitor dm;
    ZooKeeper zk;
    String fileName;
    String[] exec;
    Process child;

    public ZookeeperExecutor(String hostPort, String znode, String fileName, String[] exec) throws IOException {
        this.fileName = fileName;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
    }

    @Override
    public void run() {
        synchronized (this) {
            while (!dm.dead) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        dm.process(watchedEvent);
    }

    static class StreamWriter extends Thread {
        InputStream is;
        OutputStream os;

        public StreamWriter(InputStream is, OutputStream os) {
            this.is = is;
            this.os = os;
            start();
        }

        @Override
        public void run() {
            byte[] b = new byte[80];
            int rc;
            try {
                while ((rc = is.read(b)) > 0) {
                    os.write(b, 0, rc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exists(byte[] data) {
        //if the data exists
        //whether the process exists
        if (data == null) {
            if (child != null) {
                System.out.println("Killing process");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
            child = null;
        } else {
            if (child != null) {
                System.out.println("Stopping child");
                child.destroy();
                try {
                    child.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                System.out.println("Starting child");
                child = Runtime.getRuntime().exec(exec);
                new StreamWriter(child.getInputStream(), System.out);
                new StreamWriter(child.getErrorStream(), System.err);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void closing(int rc) {
        synchronized (this) {
            notifyAll();
        }
    }

    public static void main(String[] args) {
        try {
            new ZookeeperExecutor("172.21.139.150:2181", "/executor", "e1", new String[]{"temp1", "temp2"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
