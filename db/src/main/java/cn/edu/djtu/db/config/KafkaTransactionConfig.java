package cn.edu.djtu.db.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 * @ClassName KafkaTransactionConfig
 * @Description: TODO
 * @Author zzx
 * @Date 2022/9/22
 **/
@Configuration
public class KafkaTransactionConfig {
    @Bean
    public Map<String, Object> producerFactoryTransactionalConfigs() {
        return Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092", 
                ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, "1000", 
                ProducerConfig.ACKS_CONFIG, "all", 
                // If without this, the transaction will not function properly.
                ProducerConfig.TRANSACTIONAL_ID_CONFIG, "weCom_", 
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    }

    /**
     * No need to invoke ProducerFactory.createProducer() method, just use Kafka template through same factory and config
     * @return ProducerFactory
     */
    @Bean
    public ProducerFactory<String, String> producerTransactionalFactory() {
        return new DefaultKafkaProducerFactory<>(producerFactoryTransactionalConfigs());
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTransactionalTemplate() {
        return new KafkaTemplate<>(producerTransactionalFactory());
    }
    
    
}
