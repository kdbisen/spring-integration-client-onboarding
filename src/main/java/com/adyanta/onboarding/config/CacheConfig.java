package com.adyanta.onboarding.config;

import com.adyanta.onboarding.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cache Configuration for JWT Token caching
 */
@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
public class CacheConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    /**
     * Redis template for JWT token caching
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache manager for JWT tokens
     */
    @Bean
    public CacheManager cacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                    org.springframework.data.redis.cache.RedisCacheConfiguration
                        .defaultCacheConfig()
                        .entryTtl(java.time.Duration.ofMinutes(30)) // Default TTL
                )
                .build();
    }

    /**
     * Fallback cache manager for when Redis is not available
     */
    @Bean
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager("jwtTokens");
    }
}
