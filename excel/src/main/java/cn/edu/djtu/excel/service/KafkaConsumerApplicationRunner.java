package cn.edu.djtu.excel.service;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * @ClassName KafkaConsumerApplicationRunner
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/27
 **/
@Component
public class KafkaConsumerApplicationRunner implements ApplicationRunner {
    private final KafkaConsumer<String, String> transactionalConsumer;

    public KafkaConsumerApplicationRunner(KafkaConsumer<String, String> transactionalConsumer) {
        this.transactionalConsumer = transactionalConsumer;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        transactionalConsumer.subscribe(Collections.singletonList("weCom_customer_transactional"));
//        transactionalConsumer.subscribe(Pattern.compile("^weCom_customer_transactional.*$"));
        ConsumerRecords<String, String> consumerRecords = transactionalConsumer.poll(Duration.ofSeconds(1));
        System.out.println(consumerRecords);
    }
}
