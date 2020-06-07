package cn.edu.djtu.db;

import cn.edu.djtu.db.config.RedisConfig;
import cn.edu.djtu.db.controller.CustomerController;
import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.entity.Gender;
import cn.edu.djtu.db.service.CustomerService;
import org.apache.catalina.core.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DbApplicationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerController customerController;

    @Test
    void contextLoads() {
    }
    
    @Test
    void batchInsertTest() throws Exception {
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            Customer customer = new Customer();
            customer.setBirthday(LocalDate.of(1889,3,12));
            customer.setCellphone("18240853757");
            customer.setCompany("Google Company");
            customer.setGender(Gender.MALE);
            customer.setGmtCreate(LocalDateTime.now());
            customer.setIsDeleted(0);
            customer.setName("Tom Smith" + i+1);
            customer.setRemarks("Work for 3 years");
        }
        mockMvc.perform(post("/customers", customers)).andExpect(status().isOk());
        new HashMap<>(11);
    }
    

}
