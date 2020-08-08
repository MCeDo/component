package com.example.component.config;

import com.example.component.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Author：cedo
 * Date：2020/8/8 15:57
 */
@Configuration
public class JedisConfig {
    @Bean("jedis.config")
    public JedisPoolConfig jedisPoolConfig(//
                                           @Value("${jedis.pool.min-idle}") int minIdle,
                                           @Value("${jedis.pool.max-idle}") int maxIdle,
                                           @Value("${jedis.pool.max-wait}") int maxWaitMillis,
                                           @Value("${jedis.pool.block-when-exhausted}") boolean blockWhenExhausted,
                                           @Value("${jedis.pool.max-total}") int maxTotal) {

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(minIdle);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMaxTotal(maxTotal);
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        config.setBlockWhenExhausted(blockWhenExhausted);
        // 是否启用pool的jmx管理功能, 默认true
        config.setJmxEnabled(true);
        return config;
    }

    @Bean
    public JedisPool jedisPool(
                               @Qualifier("jedis.config") JedisPoolConfig config,
                               @Value("${jedis.host}") String host,
                               @Value("${jedis.port}") int port) {
        JedisPool jedisPool = new JedisPool(config, host, port);
        RedisUtil.setJedisPool(jedisPool);
        return jedisPool;
    }

}
