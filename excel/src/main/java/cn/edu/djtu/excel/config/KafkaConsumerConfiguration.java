package cn.edu.djtu.excel.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 * @ClassName KafkaConfiguration
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/24
 **/
@Configuration
@EnableKafka
public class KafkaConfiguration {
    @Bean
    public Map<String, Object> consumerConfigs() {
        return Map.of(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false, ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class, 
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    }
    
    @Bean
    public KafkaConsumer<String, String> transactionalConsumer() {
        return new KafkaConsumer<>(consumerConfigs());
    }
}
