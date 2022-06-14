package cn.edu.djtu.db;

import cn.edu.djtu.db.config.RedisConfig;
import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.entity.Gender;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName BasicTest
 * @Description: TODO
 * @Author zzx
 * @Date 2020/4/9
 **/
@Log
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

    class TimingInvocationHandler implements InvocationHandler {
        private Object object;
        

        public TimingInvocationHandler(Object object) {
            this.object = object;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long start = System.currentTimeMillis();
            Object result = method.invoke(object, args);
            long stop = System.currentTimeMillis();
            log.info("method: " + method.getName() + " Executing time 【" + (stop - start) + "】");

            return result;
        }
    }

    @Test
    void dynamicProxyTest() {
//        Map<String, Object> mapProxy = (Map) Proxy.newProxyInstance(BasicTest.class.getClassLoader(), 
//                new Class[]{Map.class, List.class},
//                new TimingInvocationHandler(new HashMap<>()));
//        mapProxy.put("a", 203);
//        System.out.println(mapProxy.get("a"));
        
        List<String> listProxy = (List) Proxy.newProxyInstance(BasicTest.class.getClassLoader(), 
                new Class[]{List.class},
                new TimingInvocationHandler(new LinkedList<>()));
        listProxy.add("Sam");
        System.out.println(listProxy.get(0));
    }

    @Test
    void methodInvokeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Subject.class;
        Method method = clazz.getMethod("request");
        method.invoke(new ProxySubject(), null);
    }
    
    @Test
    void springAopTest() {
        ProxyFactory factory = new ProxyFactory();
    }

    @Test
    void jdbcTransactionTest() throws SQLException {
        String url = "";
        Connection connection = DriverManager.getConnection(url);
        try (connection) {
          connection.setAutoCommit(false);
          connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
          connection.setSavepoint("barrier1");
          connection.commit();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException troubles) {
                troubles.printStackTrace();
            }
        }
    }

    @Test
    void transactionTemplateTest() {
        TransactionTemplate template = new TransactionTemplate();
        template.execute(callback -> {
            return null;
        });
    }
}
