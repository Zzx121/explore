package cn.edu.djtu.db;

import cn.edu.djtu.db.config.RedisConfig;
import cn.edu.djtu.db.entity.Customer;
import org.apache.catalina.core.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName BasicTest
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/9
 **/
public class BasicTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    
    @Test
    void dateTimeTest() {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(localDateTime);
    }
    
    @Test
   void listClassNameTest() {
       List<Customer> customers = new ArrayList<>();
       System.out.println(customers.getClass().getGenericSuperclass().getTypeName());
   }
   
   @Test
   void dateTimeFormatter() {
       LocalDateTime localDateTime = LocalDateTime.now();
       String timeString = localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
       System.out.println(timeString);
    }
    
    void faultyMethod(List<String>... l) {
        Object[] objects = l;
        objects[0] = Arrays.asList(42);
        String s = l[0].get(0);
    }
}
