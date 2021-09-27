package com.pandy.gulimall.order.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @Author Pandy
 * @Date 2021/9/4 17:57
 */
@Configuration
public class MyRedissonConfig {


    /**
     * 所有对redis的操作都是通过RedisClient对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() throws IOException {
        // 创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://47.95.216.30:6379");

        config.useSingleServer().setConnectionMinimumIdleSize(10);

        // 根据config创建出的
        return Redisson.create(config);
    }
}
