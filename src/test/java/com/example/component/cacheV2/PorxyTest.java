package com.example.component.cacheV2;

import com.example.component.cache.BaseTest;
import com.example.component.util.RedisUtil;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Proxy;

/**
 * Author：cedo
 * Date：2020/8/9 23:22
 */
public class PorxyTest extends BaseTest {

    @Test
    public void testProxy() {
        TwinTurboCacheV2 cacheV2 = (TwinTurboCacheV2) RedisUtil.getJedis();
        String test = cacheV2.get("test");
        System.out.println(test);
    }
}
