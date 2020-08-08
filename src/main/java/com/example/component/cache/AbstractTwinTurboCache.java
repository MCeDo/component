package com.example.component.cache;

import com.alibaba.fastjson.JSONObject;
import com.example.component.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

/**
 * 双涡轮缓存组件抽象类
 * TODO 是否进行读写操作分离
 * Author：cedo
 * Date：2020/8/8 15:06
 */
public abstract class AbstractTwinTurboCache {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractTwinTurboCache.class);

    /**
     * 缓存版本A key后缀
     */
    private static final String CACHE_A = ".cacheA";

    /**
     * 缓存版本B key后缀
     */
    private static final String CACHE_B = ".cacheB";

    /**
     * 版本缓存状态信息缓存 key后缀
     */
    private static final String CACHE_CUR = ".curCache";

    /**
     * 缓存更新锁后缀
     */
    private static final String LOCK = ".lock";

    /**
     * redis物理缓存过期时间
     */
    private int realExpireSecond;

    private CacheRebuildExecutor cacheRebuildExecutor;

    /**
     * 切换生效中的版本key
     * @param cacheKey
     */
    public TwinTurboKey switchCacheKey(String cacheKey) {
        TwinTurboKey curCacheKey = this.getCurCacheKey(cacheKey);
        if (curCacheKey == null) {
            // 缓存失效，默认选择版本A
            return TwinTurboKey.builder()
                    .curKey(cacheKey + CACHE_A)
                    .build();
        }
        LOGGER.info("switchCacheKey(cacheKey) method invoke, curCacheKey={}", JSONObject.toJSONString(curCacheKey));
        return this.switchCacheKey(cacheKey, curCacheKey);
    }

    /**
     * 切换生效中的版本key
     * @param cacheKey
     */
    public TwinTurboKey switchCacheKey(String cacheKey, TwinTurboKey curCacheKey) {
        // 读取当前生效中的key
        String switchKey = curCacheKey.getCurKey().equals(cacheKey + CACHE_A)
                ? cacheKey + CACHE_B
                : cacheKey + CACHE_A;
        curCacheKey.setCurKey(switchKey);
        return curCacheKey;
    }

    /**
     * 更新版本缓存Key
     * @param cacheKey
     * @param curCacheKey
     */
    public void updateTurboKey(String cacheKey, TwinTurboKey curCacheKey) {
        // TODO 加锁
        curCacheKey.setUpdateTime(System.currentTimeMillis());
        LOGGER.info("updateTurboKey method invoke, cacheKey={}, curCacheKey={}",
                cacheKey,
                JSONObject.toJSONString(curCacheKey));
        // 物理缓存有效期续期,版本key有效期加多5s，保证比缓存子key如：比cache_a有效期长
        getJedis().setex(cacheKey + CACHE_CUR, realExpireSecond + 5, JSONObject.toJSONString(curCacheKey));
    }

    /**
     * 获取当前生效中的缓存指向Key
     * @param cacheKey  业务方缓存Key
     * @return
     */
    public TwinTurboKey getCurCacheKey(String cacheKey) {
        String curCacheKey = cacheKey + CACHE_CUR;
        String value = getJedis().get(curCacheKey);
        LOGGER.info("getCurCacheKey method invoke, cacheKey={}, twinTurboKey={}",
                cacheKey,
                value);
        return StringUtils.isEmpty(value)
                ? null
                : JSONObject.parseObject(value, TwinTurboKey.class);
    }

    protected void asyncRebuildCacheAndSwitch(String cacheKey, TurboCacheResult turboCacheResult) {
        TwinTurboKey curOperateTurboKey = TwinTurboKey.builder()
                .curKey(turboCacheResult.getTwinTurboKey().getCurKey())
                .expireSeconds(turboCacheResult.getTwinTurboKey().getExpireSeconds())
                .updateTime(turboCacheResult.getTwinTurboKey().getUpdateTime())
                .build();
        // 版本缓存过期，异步重建缓存
        if (turboCacheResult.isExpire() && cacheRebuildExecutor != null) {
            // TODO 加锁
            // TODO 实现异步
            LOGGER.info("异步更新缓存并切换版本key invoke, cacheKey={}, turboCacheResult={}",
                    cacheKey,
                    JSONObject.toJSONString(turboCacheResult));
            TwinTurboKey twinTurboKey = this.switchCacheKey(cacheKey, curOperateTurboKey);
            LOGGER.info("get twinTurboKey and rebuild cache：cacheKey={}, twinTurboKey={}",
                    cacheKey,
                    JSONObject.toJSONString(twinTurboKey));
            this.rebuildCache(twinTurboKey, cacheRebuildExecutor.rebuildCache());
            this.updateTurboKey(cacheKey, twinTurboKey);
        }
    }

    /**
     * 设置redis缓存数据抽象方法，具体调用的redis由子类实现
     * @param data
     */
    protected abstract void rebuildCache(TwinTurboKey turboKey, Object data);

    protected Jedis getJedis() {
        return RedisUtil.getJedis();
    }

    public AbstractTwinTurboCache(int realExpireSecond, CacheRebuildExecutor cacheRebuildExecutor) {
        this.realExpireSecond = realExpireSecond;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    public AbstractTwinTurboCache(int realExpireSecond) {
        this.realExpireSecond = realExpireSecond;
    }

    public int getRealExpireSecond() {
        return this.realExpireSecond;
    }
}
