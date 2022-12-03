package cn.edu.djtu.excel.service;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @ClassName KafkaConsumerService
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/24
 **/
@Service
public class KafkaConsumerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaConsumer<String, String> transactionalConsumer;

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate, KafkaConsumer<String, String> transactionalConsumer) {
        this.kafkaTemplate = kafkaTemplate;
        this.transactionalConsumer = transactionalConsumer;
    }

    @KafkaListener(topics = {"weCom_customer_transactional"}, containerFactory = "")
//    @Transactional
//    @Bean
    public void userConsumer() {
        // Group id how to define
        // How to register the consumer into the listener way to run
        // Transaction id, offset, topic save and observing
        // Rollback when local transaction fails
        // Retry after rollback, that is the rolled back message retrieve again 
        // Producer group id provides *
        //transactionalConsumer.subscribe(Pattern.compile("^weCom_customer_transactiona3l.*$"));
        transactionalConsumer.subscribe(Collections.singletonList("weCom_customer_transactional"));
        ConsumerRecords<String, String> consumerRecords = transactionalConsumer.poll(Duration.ofSeconds(1));
        String transactionIdPrefix = kafkaTemplate.getTransactionIdPrefix();
    }
}
