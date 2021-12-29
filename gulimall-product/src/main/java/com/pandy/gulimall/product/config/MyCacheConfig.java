package com.pandy.gulimall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author Pandy
 * @Date 2021/9/5 2:10
 * 缓存配置管理器 自动配置了redis cache manager初始化所有缓存， 每个缓存决定了使用什么配置
 */
@EnableConfigurationProperties
@Configuration
@EnableCaching
public class MyCacheConfig {

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
//        redisCacheConfiguration = redisCacheConfiguration.entryTtl();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        CacheProperties.Redis redisproperties = cacheProperties.getRedis();
        // 设置配置
        if(redisproperties.getTimeToLive() != null){
            config = config.entryTtl(redisproperties.getTimeToLive());
        }
        if(redisproperties.getKeyPrefix() != null){
            config = config.prefixKeysWith(redisproperties.getKeyPrefix());
        }
        if(!redisproperties.isCacheNullValues()){
            config = config.disableCachingNullValues();
        }
        if(!redisproperties.isUseKeyPrefix()){
            config = config.disableKeyPrefix();
        }

        return config;
    }
}
