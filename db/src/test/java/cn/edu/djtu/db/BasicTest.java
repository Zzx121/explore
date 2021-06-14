package cn.edu.djtu.db;

import cn.edu.djtu.db.config.RedisConfig;
import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.entity.Gender;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    
    @Test
    void streamDistinctTest() {
        Customer c = new Customer();
        c.setId(10);
        c.setGender(Gender.FEMALE);
        List<Customer> customers = new ArrayList<>();
        customers.add(c);
        c = new Customer();
        c.setId(10);
        c.setGender(Gender.MALE);
        customers.add(c);
        List<Integer> ids = customers.stream().map(Customer::getId).distinct().collect(Collectors.toList());
        System.out.println(ids);
        
    }
    
    @Test
    void regTest() {
        String reg = "^\\w{4,12}$";
        Pattern compile = Pattern.compile(reg);
        Matcher m = compile.matcher("d2好dc");
        System.out.println(m.find());
    }
    
    @Test
    void pkcs8Test() {
        
    }
}
