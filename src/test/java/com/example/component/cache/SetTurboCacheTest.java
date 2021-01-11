package com.example.component.cache;

import com.alibaba.fastjson.JSONObject;
import com.example.component.cache.set.SetTurboCache;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author：cedo
 * Date：2021/1/11 22:49
 */
public class SetTurboCacheTest extends BaseTest {

    @Test
    public void test() throws InterruptedException {
        String testKey = "setTurboCacheTestV2";
        SetTurboCache turboCache = new SetTurboCache(testKey, 15, 5, () -> {
            Map<String, Double> data = new HashMap<>();
            data.put("test1", 1d);
            data.put("test2", 1d);
            data.put("test3" + System.currentTimeMillis(), 1d);
            return data;
        });
        while(true) {
            TurboCacheResult<Set<String>> result = turboCache.zrange(0, -1);
            System.out.println("SetTurboCache get方法测试，逻辑缓存是否失效 " + (result == null || result.isExpire()));

            if (result == null) {
                // 整体缓存过期，同步重建
                System.out.println("SetTurboCache 整体缓存过期 ");
                turboCache.init();
                result = turboCache.zrange(0, -1);
            }
            System.out.println("SetTurboCache get方法测试，result ==> " + JSONObject.toJSONString(result));
            Thread.sleep(1000);
        }
    }
}
