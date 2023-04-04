package cn.edu.djtu.db;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author zzx
 * @date 2022/4/19
 */
public class ZookeeperTest {
//    String address = "127.0.0.1:2181";
//    String address = "172.21.128.1:2181";
    // WSL2 use "#ip route" to get
//    String address = "172.21.139.150:2181";
    String address = "101.42.159.16:2181";
//    String address = "DESKTOP-1ON7TVC.local:2181";
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
            ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> {

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
    void checkNode() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> {
            });
            System.out.println("Stat of node ---" + zooKeeper.exists("/n4", false));
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void concurrentCreateNode() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> {
            });
//            zooKeeper.create(CreateMode.EPHEMERAL)
            System.out.println("Stat of node ---" + zooKeeper.exists("/n4", false));
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
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
    
    String lockKey;
    @Test
    void exclusiveLock() {
        //The implementation in zookeeper maybe different, here need to deal with the concurrency of the invocation(just
        //have some tests on that)
        //The key points that applied in the redis lock also can be referred here, like the atomicity and the uniqueness 
        //and also the renewal of the lock, just one by one to set up the possible solutions:
        //1) Atomicity: through locks in java
        //2) Character using to achieve this, first create the znode acquire the lock(better with unique id)
        //3) The timeout to avoid the deadlock and then the earlier release of the lock dealing
        String lockSpace = "/lockSpace";
        try {
            ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> {
                System.out.printf("【State】 %s, 【Type】 %s", event.getState().toString(), event.getType().toString());
            });
            lockKey = UUID.randomUUID().toString();
            Stat existsStat = zooKeeper.exists(String.join("_", lockSpace, lockSpace), event -> {
                System.out.printf("Exists watch【State】 %s, 【Type】 %s", event.getState().toString(), event.getType().toString());
            });
            if (existsStat != null) {
                System.out.println(existsStat);
            }
        } catch (IOException | KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        //The come out of the idea to implement the zookeeper lock is not far away from the best practice: just no polling and 
        //timeout; and just another aspect of implementation: sequential number to decide the acquired client, and the 
        //solution for the node created and the notification arrives late due to server hang up, just thing about the 
        //process first by myself
        // 1) The usage of the Watches, when to use and when not to use
        // 2) Create the child node in the lock node -> getChildren and compare to ensure that is the lowest sequence to 
        // decide whether acquired the lock: if is, use the watcher to occupy the lock, otherwise wait through the Watcher
        // 3) The key here is just the usage of the Watches
        // 4) Some questions: 1. Whether you need synchronization in the client 2. How to achieve the exclusive
        // 3. Why need to under a lock space(just simulate the process not under the namespace): A 
    }
    
    @Test
    void concurrentCreate() throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> System.out.printf("【State】 %s, 【Type】 %s", event.getState().toString(), event.getType().toString()));

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Callable> executingTasks = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            executorService.execute(() -> {
                try {
                    zooKeeper.create("/concurrent/c1", new byte[0], null, CreateMode.EPHEMERAL);
                    zooKeeper.create("/concurrent/c2", new byte[1], null, CreateMode.EPHEMERAL);
                } catch (KeeperException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
    }
    
    @Test
    void duplicatedCreate() throws IOException {
        ZooKeeper zooKeeper = new ZooKeeper(address, 3000, event -> System.out.printf("【State】 %s, 【Type】 %s", event.getState().toString(), event.getType().toString()));

        try {
            String result1 = zooKeeper.create("/tmp/duplicate1", new byte[1], List.of(new ACL()), CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(result1);
            String result2 = zooKeeper.create("/tmp/duplicate1", new byte[1], List.of(new ACL()), CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(result2);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
