package com.example.component.cache.set;

import com.example.component.cache.AbstractTwinTurboCache;
import com.example.component.cache.CacheRebuildExecutor;
import com.example.component.cache.TurboCacheResult;
import com.example.component.cache.TwinTurboKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SortedSet
 * Author：cedo
 * Date：2021/1/11 22:37
 */
public class SetTurboCache extends AbstractTwinTurboCache<Map<String, Double>> {

    public SetTurboCache(String cacheKey, int realExpireSecond, int triggerRefreshSeconds, CacheRebuildExecutor cacheRebuildExecutor) {
        super(cacheKey, realExpireSecond, triggerRefreshSeconds, cacheRebuildExecutor);
    }

    public SetTurboCache(int realExpireSecond) {
        super(realExpireSecond);
    }

    public TurboCacheResult<Set<String>> zrange(int start, int end) {
        TwinTurboKey twinTurboKey = getCurCacheKey();
        if (twinTurboKey == null) {
            return null;
        }
        Set<String> data = getJedis().zrange(twinTurboKey.getCurKey(), start, end);
        TurboCacheResult<Set<String>> result = new TurboCacheResult<>(
                data, twinTurboKey.isExpire(), twinTurboKey
        );
        // 缓存数据是否过期，并做更新操作,需要实现CacheRebuildExecutor
        this.asyncRebuildCacheAndSwitch(result);
        return result;
    }

    @Override
    public void rebuildCache(TwinTurboKey turboKey, Map<String, Double> data) {
        getJedis().zadd(turboKey.getCurKey(), data);
        getJedis().expire(turboKey.getCurKey(), getRealExpireSecond());
    }
}
