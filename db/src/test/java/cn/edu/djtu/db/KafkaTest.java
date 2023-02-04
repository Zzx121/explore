package cn.edu.djtu.db;

import cn.edu.djtu.db.entity.Customer;
import cn.edu.djtu.db.service.KafkaTransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

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
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;


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
    @Test
    void connectToKafkaCluster() throws ExecutionException, InterruptedException {
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send("startUp", "Hello, world1!");
        SendResult<String, String> result = future.get();
        System.out.println(result);
    }

}
