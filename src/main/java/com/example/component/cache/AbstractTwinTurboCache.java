package com.example.component.cache;

import com.alibaba.fastjson.JSONObject;
import com.example.component.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 双涡轮缓存组件抽象类
 * TODO 是否进行读写操作分离
 * Author：cedo
 * Date：2020/8/8 15:06
 */
public abstract class AbstractTwinTurboCache<T>{

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
     * 业务缓存Key名称
     */
    private String cacheKey;

    /**
     * redis物理缓存过期时间
     */
    private int realExpireSecond;

    /**
     * 触发缓存刷新的间隔时间
     */
    private int triggerRefreshSeconds;

    private CacheRebuildExecutor<T> cacheRebuildExecutor;

    /**
     * 切换生效中的版本key
     */
    public TwinTurboKey switchCacheKey() {
        // 读取当前生效中的key
        TwinTurboKey curCacheKey = this.getCurCacheKey();
        if (curCacheKey == null) {
            // 缓存失效，默认选择版本A
            return TwinTurboKey.builder()
                    .curKey(cacheKey + CACHE_A)
                    .build();
        }
        LOGGER.info("switchCacheKey() method invoke, curCacheKey={}", JSONObject.toJSONString(curCacheKey));
        String switchKey = curCacheKey.getCurKey().equals(cacheKey + CACHE_A)
                ? cacheKey + CACHE_B
                : cacheKey + CACHE_A;
        curCacheKey.setCurKey(switchKey);
        return curCacheKey;
    }

    /**
     * 更新版本缓存Key
     * @param curCacheKey
     */
    public void updateTurboKey(TwinTurboKey curCacheKey) {
        // TODO 加锁，此处不加锁，由调用方控制处理加锁
        curCacheKey.setUpdateTime(System.currentTimeMillis());
        LOGGER.info("updateTurboKey method invoke, cacheKey={}, curCacheKey={}",
                cacheKey,
                JSONObject.toJSONString(curCacheKey));
        // 物理缓存有效期续期,版本key有效期加-1s，保证缓存子key比版本缓存有效期长如：cache_a比curCacheKey有效期长
        getJedis().setex(cacheKey + CACHE_CUR, realExpireSecond - 1, JSONObject.toJSONString(curCacheKey));
    }

    /**
     * 获取当前生效中的缓存指向Key
     * @return
     */
    public TwinTurboKey getCurCacheKey() {
        String curCacheKey = cacheKey + CACHE_CUR;
        String value = getJedis().get(curCacheKey);
        LOGGER.info("getCurCacheKey method invoke, cacheKey={}, twinTurboKey={}",
                cacheKey,
                value);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        TwinTurboKey twinTurboKey = JSONObject.parseObject(value, TwinTurboKey.class);
        // 过期时间统一在此处判断，确保整个生命周期对过期的判断一致
        twinTurboKey.setExpire();
        return twinTurboKey;
    }

    /**
     * 组装结果Bean，同时校验是否触发更新
     * @param data
     * @param twinTurboKey
     * @return
     */
    protected TurboCacheResult<T> buildResultAndCheckRebuild(T data, TwinTurboKey twinTurboKey) {
        TurboCacheResult<T> result = TurboCacheResult.<T>builder()
                .data(data)
                .expire(twinTurboKey.getExpire())
                .twinTurboKey(getCurCacheKey())
                .build();
        this.asyncRebuildCacheAndSwitch(result);
        return result;
    }

    protected void asyncRebuildCacheAndSwitch(TurboCacheResult turboCacheResult) {
        // 版本缓存过期，异步重建缓存
        if (turboCacheResult.isExpire() && cacheRebuildExecutor != null) {
            // TODO 加锁
            // 实现异步
            CompletableFuture.runAsync(() -> {
                LOGGER.info("{}-异步更新缓存并切换版本key invoke, cacheKey={}, turboCacheResult={}",
                        Thread.currentThread(),
                        cacheKey,
                        JSONObject.toJSONString(turboCacheResult));
                TwinTurboKey twinTurboKey = this.switchCacheKey();
                LOGGER.info("get twinTurboKey and rebuild cache：cacheKey={}, twinTurboKey={}",
                        cacheKey,
                        JSONObject.toJSONString(twinTurboKey));
                this.delCache(twinTurboKey);
                this.rebuildCache(twinTurboKey, cacheRebuildExecutor.readDataExecute());
                this.updateTurboKey(twinTurboKey);
            });
        }
    }

    protected void delCache(TwinTurboKey twinTurboKey) {
        this.getJedis().del(twinTurboKey.getCurKey());
    }

    /**
     * 整体缓存失效，初始化
     */
    public T init() {
        // TODO init lock
        TwinTurboKey twinTurboKey = TwinTurboKey.builder()
                .curKey(cacheKey + CACHE_A)
                .expireSeconds(triggerRefreshSeconds)
                .build();
        LOGGER.info("get twinTurboKey and rebuild cache：cacheKey={}, twinTurboKey={}",
                cacheKey,
                JSONObject.toJSONString(twinTurboKey));
        this.delCache(twinTurboKey);
        T data = cacheRebuildExecutor.readDataExecute();
        this.rebuildCache(twinTurboKey, data);
        this.updateTurboKey(twinTurboKey);
        return data;
    }

    /**
     * 设置redis缓存数据抽象方法，具体调用的redis由子类实现
     * @param data
     */
    public abstract void rebuildCache(TwinTurboKey turboKey, T data);

    protected Jedis getJedis() {
        return RedisUtil.getJedis();
    }

    public AbstractTwinTurboCache(String cacheKey, int realExpireSecond, int triggerRefreshSeconds, CacheRebuildExecutor cacheRebuildExecutor) {
        this.cacheKey = cacheKey;
        this.realExpireSecond = realExpireSecond;
        this.triggerRefreshSeconds = triggerRefreshSeconds;
        this.cacheRebuildExecutor = cacheRebuildExecutor;
    }

    public AbstractTwinTurboCache(int realExpireSecond) {
        this.realExpireSecond = realExpireSecond;
    }

    public int getRealExpireSecond() {
        return this.realExpireSecond;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public int getTriggerRefreshSeconds() {
        return triggerRefreshSeconds;
    }
}
