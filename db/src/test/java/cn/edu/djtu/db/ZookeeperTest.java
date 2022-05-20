package cn.edu.djtu.db;

import org.apache.zookeeper.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @author zzx
 * @date 2022/4/19
 */
public class ZookeeperTest {
    String address = "127.0.0.1:2181";
    @Test
    void barrierTest() {
        Barrier barrier = new Barrier(address, "/barrier", 3);
        try {
            boolean enter = barrier.enter();
            if (!enter) {
                System.out.println("Error entering the barrier");
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Test
    void runMultiple() throws ExecutionException, InterruptedException {
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(5, 10, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100));
        Future<?> result1 = tpe.submit(this::barrierTest);
        Future<?> result2 = tpe.submit(this::barrierTest);
        Future<?> result3 = tpe.submit(this::barrierTest);
        Future<?> result4 = tpe.submit(this::barrierTest);
        result1.get();
        result2.get();
        result3.get();
        result4.get();
    }

    @Test
    void zookeeperConnTest() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper("127.0.0.1:2181", 3000, event -> {

            });
            try {
                zooKeeper.create("/n4", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                Thread.sleep(1000);
                zooKeeper.create("/n2", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                Thread.sleep(1000);
                zooKeeper.create("/n3", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//                zooKeeper.create("/n1", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void producerTest() {
        Queue queue = new Queue(address, "/queue");
        try {
            Random r = new Random(100);
            queue.produce(r.nextInt(3));
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Test
    void consumerTest() {
        Queue queue = new Queue(address, "/queue");
        try {
            int result = queue.consume();
            System.out.println(result);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }
}