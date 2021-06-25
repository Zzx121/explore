package cn.edu.djtu.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zzx
 * @date 2021/6/12
 */
@SpringBootTest
public class RedisInActionTest {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    void testConnection() {
        System.out.println(redisTemplate.keys("*USER*"));
    }
    
    @Test
    void defineUsersAndInventory() {
//        redisTemplate.opsForHash().put();
//        redisTemplate.opsForSet().add(inventoryKey, itemId);
//        System.out.println(redisTemplate.opsForSet().isMember(inventoryKey, itemId));
        System.out.println(redisTemplate.opsForSet().remove(inventoryKey, itemId));
    }
    
    int sellerId = 27;
    String itemId = "itemA";
    String inventoryKey = "inventory:" + sellerId;
    String itemKey = itemId + "." + sellerId;
    double price = 134.2;
    
    @Test
    void listItem() {
        //模拟其中一个线程改变值，看是否抛出异常
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 1, TimeUnit.MINUTES, 
                new ArrayBlockingQueue<>(5));
        executor.execute(this::transactionalAndPipelinedListItem);
//        new Thread(this::transactionalAndPipelinedListItem).start();
        executor.execute(this::transactionalAndPipelinedListItem);
//        transactionalAndPipelinedListItem();
    }

    private void transactionalAndPipelinedListItem() {
        AtomicBoolean executed = new AtomicBoolean(false);
//        boolean executed = false;
        long end = System.currentTimeMillis() + 5000;
        redisTemplate.execute((RedisCallback<?>) connection -> {
            //在一定时间内不断重试，超时或者执行完成则结束；
            while (!executed.get()) {
                if (System.currentTimeMillis() >= end) {
                    executed.set(true);
                    break;
                }
                connection.watch(inventoryKey.getBytes());
                connection.multi();
                connection.zAdd("market:".getBytes(), price, itemKey.getBytes());
                connection.sRem(inventoryKey.getBytes(), itemId.getBytes());
                connection.exec();
                executed.set(true);
            }
            return null;
        }, true, true);
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
}
