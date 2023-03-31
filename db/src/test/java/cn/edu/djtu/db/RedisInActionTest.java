package cn.edu.djtu.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author zzx
 * @date 2021/6/12
 */
@SpringBootTest
@Slf4j
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
    
    @Autowired
    private ObjectMapper objectMapper;

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
//        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        HashOperations<String, Object, Object> stringObjectObjectHashOperations = stringRedisTemplate.opsForHash();
//        hashOperations.put(sellerKey, nameKey, "SAM");
//        hashOperations.put(sellerKey, balanceKey, 2000.00);
//
//        hashOperations.put(buyerKey, nameKey, "SMITH");
//        hashOperations.put(buyerKey, balanceKey, 495.00);

        stringObjectObjectHashOperations.put(sellerKey, nameKey, "SAM");
        stringObjectObjectHashOperations.put(sellerKey, balanceKey, "2000.00");

        stringObjectObjectHashOperations.put(buyerKey, nameKey, "SMITH");
        stringObjectObjectHashOperations.put(buyerKey, balanceKey, "495.00");
        redisTemplate.opsForZSet().add(marketKey, itemKey, price);
//        redisTemplate.opsForHash().increment(sellerKey, balanceKey, 2000.30);
//        redisTemplate.opsForHash().increment(buyerKey, balanceKey, 500.43);
//        hashOperations.put("users:2", balanceKey, 394.8);
//        System.out.println(hashOperations.get(sellerKey, balanceKey));
//        System.out.println(hashOperations.get(buyerKey, balanceKey));

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

    String semaphoreKey = "COUNTING_SEMAPHORE";

    @Transactional
    public String acquireSemaphore(int permits, long timeout) {
        String member = String.valueOf(UUID.randomUUID());
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        long timeoutMills = System.currentTimeMillis() - timeout;
        //clear timed out items
        zSetOperations.removeRangeByScore(semaphoreKey, 0, timeoutMills);
        //check rank, if larger than permits, block and remove
        // The clock consistency problem also exists, in multiple system the clock relatively slower may always
        // take precedence to acquire the lock, this is unfair;The fair and unfair in ReentrantLock is through 
        // waitNode(check waiting items at first or not)
        zSetOperations.add(semaphoreKey, member, System.currentTimeMillis());
        Long rank = zSetOperations.rank(semaphoreKey, member);
        if (rank != null) {
            if (rank > (permits - 1)) {
                zSetOperations.remove(semaphoreKey, member);
                return null;
            } else {
                return member;
            }
        }
        
        return null;
    }
    
    @Transactional
    public boolean releaseSemaphore(String member) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        Long removed = zSetOperations.remove(semaphoreKey, member);
        return removed != null && removed > 0;
    }
    
    String counterSemaphore = "semaphore:remote:counter";
    String ownerSemaphore = "semaphore:remote:owner";
    String timeoutSemaphore = "semaphore:remote:timeout";
    
    @Transactional
    public String fairAcquireSemaphore(int permits, long timeout) {
        //to avoid inconsistent lock in different system, introduce counter zSet to produce continuous time-like
        // stamps 
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Long counter = valueOperations.increment(counterSemaphore);
        if (counter != null) {
            String member = String.valueOf(UUID.randomUUID());
            // counter as owner score to ensure consistency
            zSetOperations.add(ownerSemaphore, member, counter);
            long currentTimeMillis = System.currentTimeMillis();
            long timeoutMillis = currentTimeMillis - timeout;
            // it's relatively fair but timeout itself still depend on system lock
            zSetOperations.add(timeoutSemaphore, member, currentTimeMillis);
            zSetOperations.removeRangeByScore(timeoutSemaphore, 0, timeoutMillis);
            // intersect with timeout to filter timed out items
            zSetOperations.intersectAndStore(ownerSemaphore, List.of(timeoutSemaphore), ownerSemaphore
            , RedisZSetCommands.Aggregate.SUM, RedisZSetCommands.Weights.of(1, 0));
            // check if current added owner item exceed the permits
            Long rank = zSetOperations.rank(ownerSemaphore, member);
            if (rank != null && rank <= (permits - 1)) {
                return member;
            } else {
                zSetOperations.remove(ownerSemaphore, member);
                zSetOperations.remove(timeoutSemaphore, member);
                return null;
            }
        } else {
            return null;
        }
    }

    @Transactional
    public boolean fairReleaseSemaphore(String member) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        Long timeout = zSetOperations.remove(timeoutSemaphore, member);
        Long owner = zSetOperations.remove(ownerSemaphore, member);
        return timeout != null && owner != null && timeout > 0 && owner > 0;
    }
    
    @Transactional
    public boolean fairRefreshSemaphore(String member) {
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        // there's no XX option in add operation, lua can be helpful
        DefaultRedisScript<Object> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("/lua/TimeOutLock.lua"));
        Object timedOutK = redisTemplate.execute(script, Collections.singletonList("timedOutK"), "expire in 40 s", 40);
        System.out.println(timedOutK);
        return timedOutK != null;
    }
    
    @Test
    void luaTest() {
        DefaultRedisScript<Object> script = new DefaultRedisScript<>();
        script.setResultType(Object.class);
        script.setLocation(new ClassPathResource("/lua/TimeOutLock.lua"));
        //这里需要使用string方式序列化，否则会报ERR Error running script (call to f_97cdc38cfdaa799aaf5d07131e0f0fbee262ef98):
        // @user_script:5: ERR value is not an integer or out of range 
        Object timedOutK = stringRedisTemplate.execute(script, Collections.singletonList("timedOutK"), "T1", "200");
//        Object timedOutK = redisTemplate.execute(script, Collections.singletonList("zsetKey"), "T1", new BigDecimal("20.3"));
        System.out.println(timedOutK);
    }
    
    private Object executeLua(String path, List<String> keys, Object... args) {
        DefaultRedisScript<Object> script = new DefaultRedisScript<>();
        script.setResultType(Object.class);
        script.setLocation(new ClassPathResource(path));
        //这里需要使用string方式序列化，否则会报ERR Error running script (call to f_97cdc38cfdaa799aaf5d07131e0f0fbee262ef98):
        // @user_script:5: ERR value is not an integer or out of range 
        //        Object timedOutK = redisTemplate.execute(script, Collections.singletonList("zsetKey"), "T1", new BigDecimal("20.3"));
        return stringRedisTemplate.execute(script, keys, args);
    }
    
    @Test
    void luaReleaseLock() {
        String timeoutKey = "timedOutK";
        String timeoutVal = UUID.randomUUID().toString();
        executeLua("/lua/TimeOutLock.lua", Collections.singletonList(timeoutKey), timeoutVal, "20");
        System.out.println(executeLua("/lua/ReleaseLock.lua", Collections.singletonList(timeoutKey), timeoutVal));
    }
    
    @Test
    void luaSemaphore() {
        System.out.println(executeLua("/lua/SemaphoreLock.lua", List.of(ownerSemaphore, counterSemaphore, timeoutSemaphore),
                "5", UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis()), 
                String.valueOf(1000 * 60 * 3)));
    }
    
    @Test
    void luaSemaphoreSimplified() {
        System.out.println(executeLua("/lua/SemaphoreLockSimplified.lua", List.of(ownerSemaphore),
                "5", UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis()), 
                String.valueOf(1000 * 60 * 3)));
    }
    
    @Test
    void luaAnything() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setLocation(new ClassPathResource("/lua/ZAddXX.lua"));
        //这里需要使用string方式序列化，否则会报ERR Error running script (call to f_97cdc38cfdaa799aaf5d07131e0f0fbee262ef98):
        // @user_script:5: ERR value is not an integer or out of range 
        //        Object timedOutK = redisTemplate.execute(script, Collections.singletonList("zsetKey"), "T1", new BigDecimal("20.3"));
        System.out.println(stringRedisTemplate.execute(script, Collections.singletonList("Key"), "val"));
    }
    
    @Test
    void luaFuncTest() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setLocation(new ClassPathResource("/lua/AutoComplete.lua"));
        //这里需要使用string方式序列化，否则会报ERR Error running script (call to f_97cdc38cfdaa799aaf5d07131e0f0fbee262ef98):
        // @user_script:5: ERR value is not an integer or out of range 
        //        Object timedOutK = redisTemplate.execute(script, Collections.singletonList("zsetKey"), "T1", new BigDecimal("20.3"));
        System.out.println(stringRedisTemplate.execute(script, Arrays.asList(guild, "a"), UUID.randomUUID().toString()));
    }
    
    String contactKeyPrefix = "Contact:"; 
    private void updateContact(String userId, String keyWords) {
        ListOperations<String, Object> opsForList = redisTemplate.opsForList();
        String userContactKey = contactKeyPrefix + userId;
        opsForList.remove(userContactKey, 1, keyWords);
        opsForList.leftPush(userContactKey, keyWords);
        opsForList.trim(userContactKey, 0, 99);
    }
    
    private List<Object> matchWords(String wordsPrefix, String userId) {
        ListOperations<String, Object> opsForList = redisTemplate.opsForList();
        String userContactKey = contactKeyPrefix + userId;
        List<Object> contactList = opsForList.range(userContactKey, 0, -1);
        if (contactList != null && contactList.size() > 0) {
            return contactList.stream().filter(c -> String.valueOf(c).startsWith(wordsPrefix)).collect(Collectors.toList());
        }
        
        return null;
    }
    
    private List<String> getPrefixAndSuffixInDic(String word) {
        //abbz abb{ [abc] abca abcd abcz abc{
        //aa{ ab`{ [aba] abaa abab abaz aba{
        word = word.toLowerCase();
        String dicOrderedLetters = "`abcdefghijklmnopqrstuvwxyz{";
        int length = word.length();
        if (length == 0) {
            return null;
        }
        
        String lastLetter = word.substring(length - 1);
        String prefixStr = word.substring(0, length - 1);
        char predecessorLetter = dicOrderedLetters.charAt(dicOrderedLetters.indexOf(lastLetter) - 1);
        
        return List.of(prefixStr + predecessorLetter + "{", word + "{");
    }
    
    String memberPrefix = "member:";
    String guild = "WeChat";
    @Test
    @Transactional
    void dictionaryAutoCompleteTest() {
        String guild = "WeChat";
        Set<String> result = listRangedItems("a", guild);
        System.out.println(result);
    }
    
    @Test
    void prepareDic() {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        String guild = "WeChat";
        String dicKey = memberPrefix + guild;
        Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();
        tuples.add(ZSetOperations.TypedTuple.of("a", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("abc", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("ad", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("bc", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("cab", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("dfb", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("ec", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("fac", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("jams", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("smith", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("willian", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("deliberate", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("shrewdness", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("guild", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("hello", 0D));
        tuples.add(ZSetOperations.TypedTuple.of("world", 0D));
        zSetOperations.add(dicKey, tuples);
    }
    
    private Set<String> listRangedItems(String keyWord, String guild) {
        List<String> prefixAndSuffixInDic = getPrefixAndSuffixInDic(keyWord);
        if (prefixAndSuffixInDic == null || prefixAndSuffixInDic.size() == 0) {
            return null;
        }
        String identifier = UUID.randomUUID().toString();
        String prefix = prefixAndSuffixInDic.get(0) + identifier;
        String suffix = prefixAndSuffixInDic.get(1) + identifier;
        String dicKey = memberPrefix + guild;
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        zSetOperations.add(dicKey, prefix, 0);
        zSetOperations.add(dicKey, suffix, 0);

        //this way may return lots of items, so need to shrink the range
        Set<String> result = zSetOperations.rangeByLex(dicKey, RedisZSetCommands.Range.range().gt(prefix).lt(suffix));
//        Long start = zSetOperations.rank(dicKey, prefix);
//        Long end = zSetOperations.rank(dicKey, suffix);
//        //simulate to determine the number from not matched and one matched and so on
//        //this way will return at most 10 matched items
//        long shrinkedEnd = Math.min(start + 9, end - 2);
//        zSetOperations.remove(dicKey, prefix, suffix);
//        return zSetOperations.range(dicKey, start, shrinkedEnd);
        zSetOperations.remove(dicKey, prefix, suffix);
        return result;
    }
    
    @Test
    void marketLuaTest() {
        System.out.println(executeLua("/lua/MarketPurchase.lua", Arrays.asList("SAM", "SMITH"), "ItemAB", "383.13"));
    }
    
    @Test
    void counterRecordTest() {
        recordCounter("hits", 15);
        recordCounter("login", 5);
        recordCounter("transfer", 9);
//        getCounter("hits", 300);
//        cleanCounters();
    }
    private List<Integer> precisionList = Arrays.asList(5, 30, 60, 300, 600, 1800, 3600, 3600 * 6, 3600 * 24, 3600 * 24 * 30);
    private String counterKeyPrefix = "COUNTER:";
    private String knownKey = "KNOWN:";
    private void recordCounter(String counterCategory, int hits) {
        precisionList.forEach(p -> {
            long epochSecond = Instant.now().getEpochSecond();
            long secondStart = epochSecond / p * p;
            String categoryKey = p + ":" + counterCategory;
            redisTemplate.opsForHash().increment(counterKeyPrefix + categoryKey, secondStart, hits);
            stringRedisTemplate.opsForZSet().add(knownKey, categoryKey, 0);
        });
    }
    
    private void getCounter(String counterCategory, int precision) {
        String key = counterKeyPrefix + precision + ":" + counterCategory;
        Set<Object> keys = redisTemplate.opsForHash().keys(key);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        System.out.println(keys);
        System.out.println(entries);
    }

    /**
     * Because the hash don't have expiration for the subHashKey, so the solution is through zset
     * Control the clean interval just same as the putting interval and at most 1 minutes a time
     */
    private void cleanCounters(int itemsRetain) throws InterruptedException {
        //need to do transaction or pipeline
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        int passes = 0;
        int index = 0;
        Long size = zSetOperations.zCard(knownKey);
        while (size != null && index < size) {
            passes++;
            Set<String> knownCounterSet = zSetOperations.range(knownKey, index, index);
            if (knownCounterSet != null && knownCounterSet.size() > 0) {
                String knownCounter = knownCounterSet.iterator().next();
                if (knownCounter != null) {
                    String[] counterSplits = knownCounter.split(":");
                    if (counterSplits.length > 0) {
                        String precisionStr = counterSplits[0];
                        
                        int precision = Integer.parseInt(precisionStr);
                        //precision less than 1 minutes just clean
                        if (precision < 60 || (passes * 60 % precision == 0)) {
                            if (index == size - 1) {
                                index = 0;
                            } else {
                                index++;
                            }
                            String counterKey = counterKeyPrefix + knownCounter;
                            Set<Object> keys = hashOperations.keys(counterKey);
                            if (keys.size() > 0) {
                                List<Object> keysList = keys.stream().sorted().collect(Collectors.toList());
                                int listSize = keysList.size();
                                if (listSize > itemsRetain) {
                                    List<Object> deletingItems = keysList.subList(itemsRetain - 1, listSize);
                                    hashOperations.delete(counterKey, deletingItems);
                                }
                            } else {
                                zSetOperations.remove(knownKey, knownCounter);
                                index--;
                            }
                        } else {
                            continue;
                        }
//                        hashOperations.get(counterKey)
                    }
                }
            }
           
            Thread.sleep(60000);
        }
    }
    
    @Test
    void divisionTest() {
        System.out.println(30 / 60);
        System.out.println(30 % 60);
        System.out.println(redisTemplate.opsForHash().keys("COUNTER:60:transfer").stream().sorted().collect(Collectors.toList()).subList(118, 394));
    }
    
    private final String statsPrefix = "stats:";

    /**
     * statistics by hour
     * @param context e.g. profile
     * @param type e.g. access time
     * @param value value
     */
    private void updateStats(String context, String type, Double value) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        String hourAfter = dateTimeFormatter.format(LocalDateTime.now().plusHours(1));
        System.out.println(hourAfter);
        String hourNow = dateTimeFormatter.format(LocalDateTime.now());
        System.out.println(hourNow);
        System.out.println(LocalDateTime.parse(hourAfter, dateTimeFormatter).isAfter(LocalDateTime.parse(hourNow, dateTimeFormatter)));
        String statsKey = statsPrefix + context + ":" + type;
        String startKey = statsKey + ":start";
        ZSetOperations<String, Object> zSetOperations = redisTemplate.opsForZSet();
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Object startVal = valueOperations.get(startKey);
        if (startVal != null && LocalDateTime.parse((CharSequence) startVal, dateTimeFormatter).isBefore(LocalDateTime.parse(hourNow, dateTimeFormatter))) {
            redisTemplate.rename(startKey, statsKey + ":preStart");
            redisTemplate.rename(statsKey, statsKey + ":last");
            valueOperations.set(startKey, hourNow);
        } 
        String statsMin = statsKey + ":min";
        String statsMax = statsKey + ":max";
        zSetOperations.add(statsMin, "min", value);
        zSetOperations.add(statsMax, "max", value);
        zSetOperations.unionAndStore(statsKey, Collections.singleton(statsMin), statsKey, RedisZSetCommands.Aggregate.MIN);
        zSetOperations.unionAndStore(statsKey, Collections.singleton(statsMax), statsKey, RedisZSetCommands.Aggregate.MAX);
        redisTemplate.delete(Arrays.asList(statsMax, statsMin));
        
        zSetOperations.incrementScore(statsKey, "sum", value);
        zSetOperations.incrementScore(statsKey, "count", 1);
        zSetOperations.incrementScore(statsKey, "sumsq", value * value);
    }
    
    @Test
    void statsTest() {
        updateStats("www.abc.com", "hits", 3.3D);
    }
    
    private String chatRoomPrefix = "chats:";
    private String chatUserPrefix = "seen:";
    private String chatMsgPrefix = "chatMsg:";
    private String chatIdsPrefix = "ids:chat:";
    private String msgIdsPrefix = "ids:msg:";

    


    public void createChatSession(Long chatId, String sender, String message, List<String> recipients) {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        ValueOperations<String, String> stringOperation = stringRedisTemplate.opsForValue();
        
        chatId = (chatId == null || chatId <= 0) ? stringOperation.increment(chatIdsPrefix) : chatId;
        recipients.add(sender);
        Set<ZSetOperations.TypedTuple<String>> sendersSet = new HashSet<>();
        Long finalChatId = chatId;
        recipients.forEach(r -> {
            sendersSet.add(new DefaultTypedTuple<>(r, 0D));
            //user's chat room record
            zSetOperations.add(chatUserPrefix + r, String.valueOf(finalChatId), 0D);
        });
        //chat room's user record
        zSetOperations.add(chatRoomPrefix + chatId, sendersSet);

        try {
            sendMsg(chatId, sender, message);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * just put the message in the specific chat zset
     * @param chatId
     * @param sender
     * @param message
     * @throws JsonProcessingException
     */
    public void sendMsg(Long chatId, String sender, String message) throws JsonProcessingException {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        ValueOperations<String, String> stringOperation = stringRedisTemplate.opsForValue();
        
        //id in chat 
        Long msgId = stringOperation.increment(msgIdsPrefix + chatId);
        zSetOperations.add(chatMsgPrefix + chatId, objectMapper.writeValueAsString(Msg.builder().sender(sender).
                message(message).ts(System.currentTimeMillis()).id(msgId)), msgId);
    }
    
    @Getter
    @Setter
    @Builder
    public static class Msg {
        private String sender;
        private String message;
        private Long ts;
        private Long id;
    }
    
    @Transactional
    public Map<String, Set<TypedTuple<String>>> fetchPendingMessagesForUser(String sender) {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        Map<String, Set<TypedTuple<String>>> messagesOfChat = new HashMap<>();
        Set<TypedTuple<String>> seenChats = zSetOperations.rangeWithScores(chatUserPrefix + sender, 0, -1);
        if (seenChats != null && seenChats.size() > 0) {
            seenChats.forEach(t -> {
                String chatId = t.getValue();
                if (chatId != null) {
                    Double msgId = t.getScore();
                    Set<TypedTuple<String>> remainingMessages = zSetOperations.rangeByScoreWithScores(chatMsgPrefix + chatId, msgId + 1, Integer.MAX_VALUE);
                    if (remainingMessages != null && remainingMessages.size() > 0) {
                        Optional<Double> maxMsgIdOpt = remainingMessages.stream().map(TypedTuple::getScore).max(Double::compare);
                        messagesOfChat.put(chatId, remainingMessages);
                        maxMsgIdOpt.ifPresent(maxMsgId -> {
                            zSetOperations.add(chatUserPrefix + sender, chatId, maxMsgId);
                            zSetOperations.add(chatRoomPrefix + chatId, sender, maxMsgId);
                        });
                    }
                }
            });
        }
        
        return messagesOfChat;
    }
    
    @Transactional
    public void joinChat(Long chatId, String sender) {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        ValueOperations<String, String> stringOperation = stringRedisTemplate.opsForValue();
        
        double recentMsgId = stringOperation.get(msgIdsPrefix + chatId) == null ? 0 : Double.parseDouble(Objects.requireNonNull(stringOperation.get(msgIdsPrefix + chatId)));
        zSetOperations.add(chatRoomPrefix + chatId, sender, recentMsgId);
        zSetOperations.add(chatUserPrefix + sender, String.valueOf(chatId), recentMsgId);
    }
    
    public void leaveChat(Long chatId, String sender) {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        zSetOperations.remove(chatRoomPrefix + chatId, sender);
        zSetOperations.remove(chatUserPrefix + sender, chatId);
        //clear up when the chat room is empty
        Long leftUserCount = zSetOperations.zCard(chatRoomPrefix + chatId);
        if (leftUserCount == null || leftUserCount == 0) {
            redisTemplate.delete(Arrays.asList(msgIdsPrefix + chatId, chatMsgPrefix + chatId));
        }
    }

    private String lockValue;

    //The primary task is just implement the renewal of lock's expiration time
    //1) Need to check ahead rather than the right moment
    //2) How to trigger the renewal(start after acquired the lock), and when to stop that(the lock has been
    //released by the owner(check before set the expiration))
    //3) How to check and set the expiration, through Thread(ScheduledExecutorService, advanced timers)
    public boolean lock(String usageScene) {
        String separator = "_";
        lockValue = String.join(separator, UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis()));
        String lockKey = generateLockKey(usageScene);
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(lockKey,
                lockValue, 30, TimeUnit.SECONDS);
        boolean result = Boolean.TRUE.equals(aBoolean);
        if (result) {
            renewalExpiration(lockKey);
        }
        return result;
    }

    public boolean unLock(String usageScene) {
        //Just the normal way to unlock, need to use lua in practice
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        String key = generateLockKey(usageScene);
        Object o = opsForValue.get(key);
        if (lockValue.equals(o)) {
            redisTemplate.delete(key);
            return true;
        }

        return false;
    }

    private String generateLockKey(String usageScene) {
        String appName = "Explorer";
        String separator = "_";
        return String.join(separator, appName, usageScene, UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis()));
    }

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(10);

    public void renewalExpiration(String lockKey) {
        //Just every 10s, add on 30s of expiration
        scheduled.scheduleAtFixedRate(() -> {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
                Long expirationInSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
                if (expirationInSeconds != null) {
                    log.info("【Expiration in seconds】 {}, 【lockKey】 {}", expirationInSeconds, lockKey);
                    redisTemplate.expire(lockKey, expirationInSeconds + 30, TimeUnit.SECONDS);
                }
            }
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    @Test
    void renewalTest() throws InterruptedException {
        lock("ORDERING");
        Thread.sleep(2000);
        unLock("ORDERING");
    }

}
