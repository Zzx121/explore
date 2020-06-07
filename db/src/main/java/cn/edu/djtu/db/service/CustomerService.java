package cn.edu.djtu.db.service;

import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.dao.CustomerMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.catalina.mapper.Mapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @ClassName CustomerService
 * @Description: TODO
 * @Author zzx
 * @Date 2020/3/4
 **/
@Service
public class CustomerService {
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    public void insertCustomer(Customer customer) {
        customerMapper.insertCustomer(customer);
    }
    
    public List<Customer> listAllCustomer() {
        return customerMapper.listAllCustomer();
    }

    /**
     * use <foreach> to generate a giant insertion 
     * @param customers
     */
    private void batchInsertCustomersGiantSql(List<Customer> customers) {
        customerMapper.batchInsertCustomers(customers);
    }

    /**
     * just direct loop single insert, slow
     * @param customers
     */
    private void batchInsertCustomersSimpleLoop(List<Customer> customers) {
        customers.forEach(customer -> customerMapper.insertCustomer(customer));
    }

    /**
     * set ExecutoType as BATCH when openSession, but no obvious improvement compare with simple loop
     * @param customers
     */
    private void batchInsertCustomersExecutorTypeBatch(List<Customer> customers) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            CustomerMapper mapper = session.getMapper(CustomerMapper.class);
            customers.forEach(customer -> customerMapper.insertCustomer(customer));
            session.commit();
        }
    }
    
    public void batchInsertCustomers(List<Customer> customers) throws InterruptedException {
        int threadCount = 4;
        int listSize = customers.size();
        int unitSize = listSize / threadCount;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch downSignal = new CountDownLatch(threadCount);
        for (int i = 1; i <= threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                int fromIndex = (finalI - 1) * unitSize;
                int toIndex;
                if (finalI == threadCount) {
                    toIndex = listSize - 1;
                } else {
                    toIndex = finalI * unitSize;
                }
                List<Customer> subbedList = customers.subList(fromIndex, toIndex);
//                batchInsertCustomersSimpleLoop(subbedList);
                batchInsertCustomersExecutorTypeBatch(subbedList);
//                batchInsertCustomersGiantSql(subbedList);
                downSignal.countDown();
            });
        }
        downSignal.await();
        System.out.println("done!");
    }
    
    public <T> void saveInRedis(List<T> list, Class<T> clazz) {
        String key = generateKeyName(clazz);
        Jackson2JsonRedisSerializer<T> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(clazz);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        // serialize whole List to byte[][] and save, about 0.5s for 100k 
//        redisTemplate.opsForList().rightPushAll(key, list);
        // pipeline save, about 2s-3s for 100k
        redisTemplate.execute(connection -> {
            list.forEach(c -> {
                try {
                    connection.rPush(key.getBytes(), ow.writeValueAsBytes(c));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            return null;
        }, true, true);
    }
    
    private String generateKeyName(Class<?> clazz) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String timeString = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
        
        return clazz.getSimpleName() + "_" + timeString;
    }

    private <T> Map<String, List<T>> listCustomerFromRedis(Class<?> clazz) {
        String keyPrefix = clazz.getSimpleName();
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        Map<String, List<T>> listMap = new HashMap<>(1);
        
        Objects.requireNonNull(keys).forEach(k -> {
            List<Object> customers = redisTemplate.opsForList().range(k, 0, -1);
            listMap.put(k, Objects.requireNonNull(customers).stream().map(c -> (T)c).collect(Collectors.toList()));
        });
        
        return listMap;
    }
}
