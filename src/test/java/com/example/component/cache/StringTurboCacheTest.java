package com.example.component.cache;

import com.alibaba.fastjson.JSONObject;
import com.example.component.cache.string.StringTurboCache;
import org.junit.Test;

/**
 * Author：cedo
 * Date：2020/8/9 0:44
 */
public class StringTurboCacheTest extends BaseTest {

    @Test
    public void testStringTurboCache() throws InterruptedException {
        String testKey = "stringTurboCacheTest";
        String testValue = "String类型测试";
        StringCacheRebuildExecutor stringCacheRebuildExecutor = new StringCacheRebuildExecutor();
        StringTurboCache stringCache = new StringTurboCache(testKey, 60, 5, stringCacheRebuildExecutor);
        while (true) {
            TurboCacheResult<String> result = stringCache.get();
            System.out.println("StringTurboCache get方法测试，逻辑缓存是否失效 " + (result == null || result.isExpire()));

            if (result == null) {
                // 整体缓存过期，同步重建
                System.out.println("StringTurboCache 整体缓存过期");
                stringCache.init();
                result = stringCache.get();
            }

            System.out.println("StringTurboCache get方法测试，result ==> " + JSONObject.toJSONString(result));
            Thread.sleep(1000);
        }

    }

    public static class StringCacheRebuildExecutor implements CacheRebuildExecutor<String>{
        @Override
        public String readDataExecute() {
            return "StringTurboCache 逻辑缓存失效重建测试" + System.currentTimeMillis();
        }

    }
}
