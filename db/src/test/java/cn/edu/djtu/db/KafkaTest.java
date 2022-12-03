package cn.edu.djtu.db;

import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.service.KafkaTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName KafkaTest
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/23
 **/
@SpringBootTest
public class KafkaTest {
    @Autowired
    private KafkaTransactionService kafkaTransactionService;


    @Test
    void inCaseOfKafkaFailed() {
        kafkaTransactionService.inCaseOfKafkaFailed();
    }
    
    @Test
    void transactionOrder() {
        kafkaTransactionService.transactionOrder();
    }
    
    @Test
    void eventListenerSave() {
        kafkaTransactionService.eventListenerSave();
    }
}
