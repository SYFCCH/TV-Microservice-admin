package com.syf.redis.config;


import com.syf.redis.util.RedisUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;



/**
 * Redis自动装配
 * @author syf
 */
@Configuration
@AutoConfigureBefore(RedisTemplate.class)
public class RedisAutoConfig {


    /**
     * redis工具类
     */
    @Bean("redisUtils")
    public RedisUtils redisUtils() {
        return new RedisUtils();
    }




}
