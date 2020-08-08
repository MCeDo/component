package com.example.component.cache.string;

import com.example.component.cache.AbstractTwinTurboCache;
import com.example.component.cache.CacheRebuildExecutor;
import com.example.component.cache.TurboCacheResult;
import com.example.component.cache.TwinTurboKey;

/**
 * String 双涡轮缓存工具
 * Author：cedo
 * Date：2020/8/8 17:19
 */
public class StringTurboCache extends AbstractTwinTurboCache implements StringTurboCacheCommands{

    public StringTurboCache(int realExpireSecond, CacheRebuildExecutor cacheRebuildExecutor) {
        super(realExpireSecond, cacheRebuildExecutor);
    }

    public StringTurboCache(int realExpireSecond) {
        super(realExpireSecond);
    }

    /**
     * 读取当前生效缓存
     * @param key
     * @return
     */
    @Override
    public TurboCacheResult<String> get(String key) {
        TwinTurboKey curCacheKey = this.getCurCacheKey(key);
        // 整体缓存物理过期
        if (curCacheKey == null) {
            LOGGER.info("StringTurboCache#get method invoke, key={}，整体缓存过期", key);
            return null;
        }
        String data = getJedis().get(curCacheKey.getCurKey());
        TurboCacheResult<String> result = new TurboCacheResult<>(
                data, curCacheKey
        );
        // 缓存数据是否过期，并做更新操作,需要实现CacheRebuildExecutor
        this.asyncRebuildCacheAndSwitch(key, result);
        return result;
    }

    @Override
    public void set(String key, String value, int expireSeconds) {
        // 切待更新版本key
        TwinTurboKey curCacheKey = this.switchCacheKey(key);
        curCacheKey.setExpireSeconds(expireSeconds);
        this.getJedis().setex(curCacheKey.getCurKey(), getRealExpireSecond(), value);
        // TODO 考虑自定义扩展时，是否可以对外屏蔽以下方法的调用
        this.updateTurboKey(key, curCacheKey);
    }

    @Override
    protected void rebuildCache(TwinTurboKey turboKey, Object data) {
        this.getJedis().set(turboKey.getCurKey(), (String)data);
    }
}
