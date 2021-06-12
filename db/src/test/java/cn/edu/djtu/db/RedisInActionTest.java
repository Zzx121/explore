package cn.edu.djtu.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

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
        redisTemplate.opsForSet().add("inventory:27", "itemA");
        System.out.println(redisTemplate.opsForSet().isMember("inventory:27", "itemA"));
        redisTemplate.opsForSet().remove("inventory:27", "itemA");
    }
    
    @Test
    void listItem() {
        int sellerId = 27;
        String itemId = "itemA";
        String inventoryKey = "inventory:" + sellerId;
        String itemKey = itemId + "." + sellerId;
        double price = 134.2;
        long end = System.currentTimeMillis() + 5000;
        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            while (System.currentTimeMillis() < end) {
//                connection.watch(inventoryKey.getBytes());
//                if (connection.sIsMember(inventoryKey.getBytes(), itemId.getBytes()) == null) {
//                    connection.unwatch();
//                    return null;
//                }
                connection.multi();
                connection.zAdd("market:".getBytes(), price, itemKey.getBytes());
                connection.sRem(inventoryKey.getBytes(), itemId.getBytes());
                connection.exec();
            }
            
            return null;
        });
    }
}
