package cn.edu.djtu.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @date 2021/6/12
 */
@SpringBootTest
public class RedisInActionTest {
    int sellerId = 27;
    String itemId = "itemA";
    String marketKey = "market:";
    String inventoryKey = "inventory:" + sellerId;
    String itemKey = itemId + "." + sellerId;
    double price = 234.2;
    AtomicBoolean executed = new AtomicBoolean(false);
    AtomicBoolean purchaseExecuted = new AtomicBoolean(false);
    //模拟其中一个线程改变值，看是否抛出异常
    ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(5));
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testConnection() {
        System.out.println(redisTemplate.keys("*USER*"));
    }

    @Test
    void defineUsersAndInventory() {
//        redisTemplate.opsForHash().put();
        redisTemplate.opsForSet().add(inventoryKey, itemId);
        redisTemplate.opsForZSet().remove(marketKey, itemKey);
        System.out.println(redisTemplate.opsForSet().isMember(inventoryKey, itemId));
//        System.out.println(redisTemplate.opsForSet().remove(inventoryKey, itemId));
    }

    @Test
    void listItem() throws InterruptedException, ExecutionException {
        SessionCallback<Boolean> sessionCallback = new SessionCallback<>() {
            //这里的方法中参数类型无法通过Interface推断出，所以只能使用Raw type
            @Override
            public Boolean execute(RedisOperations operations) {
                long end = System.currentTimeMillis() + 5000;
                //在一定时间内不断重试，超时或者执行完成则结束；
                while (!executed.get()) {
                    if (System.currentTimeMillis() >= end) {
                        executed.set(true);
                        break;
                    }
                    operations.watch(inventoryKey);
                    if (operations.opsForSet().isMember(inventoryKey, itemId)) {
                        operations.multi();
                        operations.opsForZSet().add(marketKey, itemKey, price);
                        operations.opsForSet().remove(inventoryKey, itemId);
                        operations.exec();
                        executed.set(true);
                        operations.unwatch();
                    }
                    executed.set(true);
                }
                return null;
            }
        };

//        new Thread(this::transactionalAndPipelinedListItem).start();
//        new Thread(this::transactionalAndPipelinedListItem).start();
        List<Callable<Object>> tasks = new ArrayList<>();
        tasks.add(Executors.callable(() -> {
            redisTemplate.execute(sessionCallback);
        }));
        tasks.add(Executors.callable(() -> {
            redisTemplate.execute(sessionCallback);
        }));
        tasks.add(Executors.callable(() -> {
            redisTemplate.execute(sessionCallback);
        }));
        tasks.add(Executors.callable(() -> {
            redisTemplate.execute(sessionCallback);
        }));
//        tasks.add(Executors.callable(this::transactionalAndPipelinedListItem));
        //junit 会在执行完当前的代码块就退出主线程，Spring的上下文环境也会关闭，需要让程序等待子线程执行完毕再关闭
        executor.invokeAll(tasks);
//        Future<?> future1 = executor.submit(this::transactionalAndPipelinedListItem);
//        Future<?> future2 = executor.submit(this::transactionalAndPipelinedListItem);
//        future1.get();
//        future2.get();
//        executor.execute(this::transactionalAndPipelinedListItem);
//        executor.execute(this::transactionalAndPipelinedListItem);
//        transactionalAndPipelinedListItem();
    }


    RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
    String usersKey = "users:";
    String sellerKey = usersKey + 10;
    String buyerKey = usersKey + 12;
    String nameKey = "name";
    String balanceKey = "balance";

    @Test
    void initUsers() {
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        HashOperations<String, Object, Object> stringObjectObjectHashOperations = stringRedisTemplate.opsForHash();
        hashOperations.put(sellerKey, nameKey, "SAM");
        hashOperations.put(sellerKey, balanceKey, 2000.00);

        hashOperations.put(buyerKey, nameKey, "SMITH");
        hashOperations.put(buyerKey, balanceKey, 495.00);

//        stringObjectObjectHashOperations.put(sellerKey, nameKey, "SAM");
//        stringObjectObjectHashOperations.put(sellerKey, balanceKey, "2000.00");
//
//        stringObjectObjectHashOperations.put(buyerKey, nameKey, "SMITH");
//        stringObjectObjectHashOperations.put(buyerKey, balanceKey, "495.00");
        redisTemplate.opsForZSet().add(marketKey, itemKey, price);
//        redisTemplate.opsForHash().increment(sellerKey, balanceKey, 2000.30);
//        redisTemplate.opsForHash().increment(buyerKey, balanceKey, 500.43);
//        hashOperations.put("users:2", balanceKey, 394.8);
        System.out.println(hashOperations.get(sellerKey, balanceKey));
        System.out.println(hashOperations.get(buyerKey, balanceKey));

        System.out.println(stringObjectObjectHashOperations.get(sellerKey, balanceKey));
        System.out.println(stringObjectObjectHashOperations.get(buyerKey, balanceKey));
        System.out.println(redisTemplate.opsForZSet().rank(marketKey, itemKey));
        System.out.println(redisTemplate.opsForZSet().rank(marketKey, "itemA.10"));
    }

    @Test
    @Transactional
    void purchaseItem() {
        // user buy things from market
        // need to ensure item exists and user's balance is sufficient
        // if condition passed, transfer money from buyer to seller, put item into buyer's inventory
        SessionCallback<Boolean> sessionCallback = new SessionCallback<>() {
            //这里的方法中参数类型无法通过Interface推断出，所以只能使用Raw type
            @Override
            public Boolean execute(RedisOperations operations) {
                long end = System.currentTimeMillis() + 5000;
                //在一定时间内不断重试，超时或者执行完成则结束；
                while (!purchaseExecuted.get()) {
                    if (System.currentTimeMillis() >= end) {
                        purchaseExecuted.set(true);
                        break;
                    }
//                    operations.watch(Arrays.asList(marketKey, buyerKey));
                    if (operations.opsForZSet().rank(marketKey, itemKey) > -1) {
//                        double balance = Double.valueOf(String.valueOf(operations.opsForHash().get(buyerKey, balanceKey)));
                        double balance = (double) operations.opsForHash().get(buyerKey, balanceKey);
                        Double price = operations.opsForZSet().score(marketKey, itemKey);
                        if (balance < price) {
                            purchaseExecuted.set(true);
//                            operations.unwatch();   
                            break;
                        }
                        try {
//                            operations.multi();
                            operations.opsForZSet().remove(marketKey, itemKey);
                            operations.opsForSet().add("inventory:12", itemKey);
                            operations.opsForHash().increment(sellerKey, balanceKey, price);
                            operations.opsForHash().increment(buyerKey, balanceKey, -price);
//                            operations.exec();
                            purchaseExecuted.set(true);
//                            operations.unwatch();
                        } catch (Exception e) {
                            e.printStackTrace();
//                            operations.discard();
//                            operations.unwatch();
                        }

                    }
                    purchaseExecuted.set(true);
                }
                return null;
            }

        };

//        redisTemplate.setHashKeySerializer(stringRedisSerializer);
//        redisTemplate.setHashValueSerializer(stringRedisSerializer);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.execute(sessionCallback);

//        List<Callable<Object>> tasks = new ArrayList<>();
//        tasks.add(Executors.callable(() -> {
//            redisTemplate.execute(sessionCallback);
//        }));
//        tasks.add(Executors.callable(() -> {
//            redisTemplate.execute(sessionCallback);
//        }));
//        tasks.add(Executors.callable(() -> {
//            redisTemplate.execute(sessionCallback);
//        }));
//        tasks.add(Executors.callable(() -> {
//            redisTemplate.execute(sessionCallback);
//        }));
////        tasks.add(Executors.callable(this::transactionalAndPipelinedListItem));
//        //junit 会在执行完当前的代码块就退出主线程，Spring的上下文环境也会关闭，需要让程序等待子线程执行完毕再关闭
//        executor.invokeAll(tasks);

    }

    @Test
    @Transactional
    void purchaseItemTransactionalWithoutSessionCallback() {
        redisTemplate.setEnableTransactionSupport(true);
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        SetOperations<String, Object> setOperations = redisTemplate.opsForSet();
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        long end = System.currentTimeMillis() + 5000;
        //在一定时间内不断重试，超时或者执行完成则结束；
        while (!purchaseExecuted.get()) {
            if (System.currentTimeMillis() >= end) {
                purchaseExecuted.set(true);
                break;
            }
            if (zSetOperations.rank(marketKey, itemKey) > -1) {
                double balance = (double) hashOperations.get(buyerKey, balanceKey);
                Double price = zSetOperations.score(marketKey, itemKey);
                if (balance < price) {
                    purchaseExecuted.set(true);
                    break;
                }
                try {
                    zSetOperations.remove(marketKey, itemKey);
                    setOperations.add("inventory:12", itemKey);
                    hashOperations.increment(sellerKey, balanceKey, price);
                    hashOperations.increment(buyerKey, balanceKey, -price);
                    purchaseExecuted.set(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            purchaseExecuted.set(true);
        }
    }

    private void transactionalAndPipelinedListItem() {

//        boolean executed = false;
        long end = System.currentTimeMillis() + 5000;
        try {
            redisTemplate.execute(new SessionCallback<Boolean>() {
                //这里的方法中参数类型无法通过Interface推断出，所以只能使用Raw type
                @Override
                public Boolean execute(RedisOperations operations) {
                    //在一定时间内不断重试，超时或者执行完成则结束；
                    while (!executed.get()) {
                        if (System.currentTimeMillis() >= end) {
                            executed.set(true);
                            break;
                        }
                        operations.watch(inventoryKey);
                        if (operations.hasKey(inventoryKey)) {
                            operations.multi();
                            operations.opsForZSet().add(marketKey, itemKey, price);
                            operations.opsForSet().remove(inventoryKey, itemId);
                            operations.exec();
                            operations.unwatch();
                            executed.set(true);
                        }
                        executed.set(true);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // connection.sRem(inventoryKey.getBytes(), itemId.getBytes());会不起作用，需要使用SessionCallback
//        redisTemplate.execute((RedisCallback<?>) connection -> {
//            //在一定时间内不断重试，超时或者执行完成则结束；
//            while (!executed.get()) {
//                if (System.currentTimeMillis() >= end) {
//                    executed.set(true);
//                    break;
//                }
//                connection.watch(inventoryKey.getBytes());
//                connection.multi();
//                connection.zAdd("market:".getBytes(), price, itemKey.getBytes());
//                connection.sRem(inventoryKey.getBytes(), itemId.getBytes());
//                connection.exec();
//                executed.set(true);
//            }
//            return null;
//        }, false, true);
    }

    @Test
    void anonymousClassTest() {
        AtomicInteger integer = new AtomicInteger(23);
        final int[] count = {0};
        new Runnable() {

            @Override
            public void run() {
                integer.incrementAndGet();
            }
        };
    }

    @Test
    void setTest() {
        int sellerId = 27;
        String itemId = "itemA";
        String inventoryKey = "inventory:" + sellerId;
        SetOperations<String, Object> opsForSet = redisTemplate.opsForSet();
        Long addCount = opsForSet.add(inventoryKey, itemId);
        System.out.println("【add count】 " + addCount);
        System.out.println("【is member】 " + opsForSet.isMember(inventoryKey, itemId));
//        System.out.println("【deleted count】 " + opsForSet.remove("inventory:27", "itemA"));
    }

    @Test
    void setNXTest() {
        String uuid = UUID.randomUUID().toString();
        Boolean lockA = stringRedisTemplate.opsForValue().setIfAbsent("lockA", uuid, 10, TimeUnit.SECONDS);
        System.out.println(lockA);
        System.out.println(uuid);
    }

    private Map<String, String> acquireLock(String lockName, long timeout) {
        // avoid the situation of release lock of others
        String identifier = UUID.randomUUID().toString();
        lockName = "lock_" + lockName;
        long end = System.currentTimeMillis() + timeout;
        Boolean acquired = null;
    
        // retry for some time
        while (System.currentTimeMillis() < end) {
            acquired = stringRedisTemplate.opsForValue().setIfAbsent(lockName, identifier, 2, TimeUnit.HOURS);
            if (acquired != null && acquired) {
                return Map.of("lockName", lockName, "identifier", identifier);
            }
        }

        return null;
    }

    /**
     * The WATCH is optimistic lock and in heavy load this will lead to high reties and latency(contention),
     * just use the normal lock will reduce retries and achieve low latency especially in heavy load, but be aware of 
     * deadlocks when partly locked.
     */
    @Test
    void acquireAndReleaseLockTest() {
        Map<String, String> lockA = acquireLock("lockC", 1000);
        System.out.println(lockA);
    }
    
    private boolean releaseLock(String lockName, String identifier) {
        // can be change to normal lock, this may also lead to many retries
        stringRedisTemplate.watch(lockName);
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String lockValue = stringStringValueOperations.get(lockName);
        // The CAS here is not atomic, can change to lua script
        if (identifier.equals(lockValue)) {
            stringRedisTemplate.multi();
            stringRedisTemplate.delete(lockName);
            stringRedisTemplate.exec();
            return true;
        } else {
            return false;
        }
    }

}
