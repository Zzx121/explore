package cn.edu.djtu.db.config;

import cn.edu.djtu.db.entity.Customer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @ClassName RedisConfig
 * @Description: TODO
 * @Author zzx
 * @Date 2020/6/3
 **/
@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("192.168.1.40", 6379));
//        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration("redis-12363.c261.us-east-1-4.ec2.cloud.redislabs.com",
//                12363);
//        redisStandaloneConfiguration.setPassword("qVlek4X1mlElDQufr5QRPQQNpSc9Qi7H");
//        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }
    
    @Bean
    public RedisTemplate<?,?> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> stringRedisSerializer = new StringRedisSerializer();
        JdkSerializationRedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(stringRedisSerializer);
        
        template.setValueSerializer(jdkSerializationRedisSerializer);
        
        return template;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException {
        return new DataSourceTransactionManager(dataSource);
    }
}