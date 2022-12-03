package cn.edu.djtu.db.service;

import cn.edu.djtu.db.dao.CustomerMapper;
import cn.edu.djtu.db.entity.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * @ClassName KafkaTransactionService
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/22
 **/
@Service
public class KafkaTransactionService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, String> kafkaTransactionalTemplate;
    private final CustomerMapper customerMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public KafkaTransactionService(KafkaTemplate<String, String> kafkaTemplate, KafkaTemplate<String, String> kafkaTransactionalTemplate, CustomerMapper customerMapper, ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTransactionalTemplate = kafkaTransactionalTemplate;
        this.customerMapper = customerMapper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }
    
    private String weComUserTopic = "weCom_user";
    private String weComCustomerTopic = "weCom_customer";
    private String weComCustomerTopicTransactional = "weCom_customer_transactional";
    
    public void sendMessage(String payload) {
        ListenableFuture<SendResult<String, String>> weComUserFuture = kafkaTemplate.send(weComUserTopic, payload);
        System.out.println(weComUserFuture);
    }
    private String userPayload = "{\"user1\": {\"name\": \"Sam\"}}";
    private String customerPayload = "{\"customer1\": {\"name\": \"Sam Smith\"}}";
    private String transactionIdPrefix = "weCom_transaction_";
    /**
     * Validates the transactional influence of Kafka failure to the DB transaction
     * 与预料的一样，如果两个在同一个事务中，假如Kafka的调用失败抛出异常，那么整个事务就会回滚；
     * 所以需要分开处理，以确保至少本地的事件保存是成功的，数据的可靠保留最重要，所以这里的事务只是保存当前服务要入库的数据和
     * 事件中转表
     */
    @Transactional
    public void inCaseOfKafkaFailed() {
        customerMapper.insertCustomer(Customer.builder().id(21093).cellphone("183940432").name("Sam").build());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException();
    }
    
    /**
     * Validates the transactional order 
     */
    @Transactional
    public void transactionOrder() {
        customerMapper.insertCustomer(Customer.builder().id(21093).cellphone("183940432").name("Sam").build());
        kafkaTemplate.send(weComUserTopic, customerPayload);
    }

    /**
     * Separation of transaction and non-transaction templates TODO @Qualifier
     */

    /**
     * Send the transactional way
     */
    @Transactional
    public void sendMessageTransactional(String payload) throws ExecutionException, InterruptedException {
        // Set a transaction id prefix to override the prefix in the producer factory.**
        kafkaTransactionalTemplate.setTransactionIdPrefix(transactionIdPrefix);
        kafkaTransactionalTemplate.inTransaction();
        ListenableFuture<SendResult<String, String>> weComUserFuture = kafkaTransactionalTemplate.send(weComCustomerTopicTransactional, payload);
        System.out.println(weComUserFuture);
        RecordMetadata recordMetadata = weComUserFuture.get().getRecordMetadata();
        long offset = recordMetadata.offset();
        long timestamp = recordMetadata.timestamp();
    }

    /**
     * Work with transactional event listener 
     */
    @Transactional
    public void eventListenerSave() {
        Customer customer = Customer.builder().id(21093019).cellphone("183940432").name("Sam19").build();
        int result = customerMapper.insertCustomer(customer);
        eventPublisher.publishEvent(customer);
//        kafkaTemplate.send(weComUserTopic, customerPayload);
    }

    /**
     * REQUIRED VS REQUIRED_NEW propagation, retries whether needed
     * @param payload
     */
    @TransactionalEventListener
    public void eventListenerSendMessage(Customer payload) throws JsonProcessingException, ExecutionException, InterruptedException {
        sendMessageTransactional(objectMapper.writeValueAsString(payload));
        System.out.println(payload);
        // Retries
    }
}
