package com.example.component.cache;

import com.example.component.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisPool;

/**
 * Author：cedo
 * Date：2020/8/8 15:18
 */
@RunWith(SpringRunner.class)
//配置本地随机端口，服务器会选择一个空闲的端口使用，避免端口冲突
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisTest {

    @Test
    public void getRedis(){
        RedisUtil.getJedis().set("test", "redis 连接测试");
        String test = RedisUtil.getJedis().get("test");
        System.out.println(test);

        RedisUtil.getJedis().del("test");
    }
}