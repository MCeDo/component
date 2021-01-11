package com.example.component.cacheV2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Author：cedo
 * Date：2020/8/9 23:14
 */
public class TwinTurboCacheHandler implements InvocationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwinTurboCacheHandler.class);

    private Jedis twinTurboCacheV2;

    public TwinTurboCacheHandler(Jedis twinTurboCacheV2) {
        this.twinTurboCacheV2 = twinTurboCacheV2;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOGGER.info("双涡轮版本缓存方法拦截，method={}", method.getName());
        before();
        method.invoke(proxy, args);
        after();
        return null;
    }

    private void after() {
        LOGGER.info("后置处理");
    }

    private void before() {
        LOGGER.info("前置处理");
    }
}
