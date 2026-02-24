package com.hometask.inventory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hometask.inventory.model.Product;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Product> redisTemplate(RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {
        RedisTemplate<String, Product> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer: Strings
        template.setKeySerializer(new StringRedisSerializer());

        // Value Serializer: JSON
        Jackson2JsonRedisSerializer<Product> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
                Product.class);
        template.setValueSerializer(serializer);

        return template;
    }
}
