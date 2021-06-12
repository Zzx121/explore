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
//        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("192.168.1.40", 6379));
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
}