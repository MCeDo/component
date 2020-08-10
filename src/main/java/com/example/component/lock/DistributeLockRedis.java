package com.example.component.lock;

import com.example.component.cache.AbstractTwinTurboCache;
import com.example.component.util.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;

public class DistributeLockRedis implements DistributeLock{

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractTwinTurboCache.class);

    public static final String NX = "NX";

    public static final String EX = "EX";

    public static final String OK = "ok";

    public static final long DEFAULT_EXPIRE_TIME = 10000;   //默认失效时间

    public static final long DEFAULT_SPIN_TIME = 5000;      //默认自旋时间

    public static final long DEFAULT_SPIN_TINTERVAL = 50;   //默认自旋间隔

    private Jedis jedis = RedisUtil.getJedis();

    private static final ThreadLocal<Integer> REENTRY_COUNT = new ThreadLocal<>();

    private static final ThreadLocal<String> UNIQUE_VALUE = new ThreadLocal<>();

    private static final String UNLOCK_LUA_SCRIPT = "if redis.call(\"get\",KEYS[1]) == ARGV[1] " +
            "then " +
            "    return redis.call(\"del\",KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end ";


    @Override
    public boolean lock(String key, long spinTime,long spinTimeInterval, long expireTime) {
        String uniqueValue = UNIQUE_VALUE.get();
        if(!StringUtils.isEmpty(uniqueValue) && isSameLock(key,uniqueValue)){
            int count = REENTRY_COUNT.get();
            REENTRY_COUNT.set(++count);
            return true;
        }
        long spinExpireTime = System.currentTimeMillis() + spinTime;
        uniqueValue = createUniqueValue();
        while(true){
            long now = System.currentTimeMillis();
            if(now > spinExpireTime){
                return false;
            }
            String result = jedis.set(key,uniqueValue,NX,EX,expireTime);
            boolean isLock = OK.equals(result);
            if(isLock){
                UNIQUE_VALUE.set(uniqueValue);
                REENTRY_COUNT.set(1);
                return true;
            }
            try {
                Thread.sleep(spinTimeInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean unLock(String key) {
        String uniqueValue = UNIQUE_VALUE.get();
        if(StringUtils.isEmpty(uniqueValue)){
            return false;
        }
        int count = REENTRY_COUNT.get();
        if(count > 1){
            REENTRY_COUNT.set(--count);
            return true;
        }
        Object result = jedis.eval(UNLOCK_LUA_SCRIPT, Collections.singletonList(key), Collections.singletonList(uniqueValue));
        boolean isUnLock = new Integer(1).equals(result);
        if(!isUnLock){
            UNIQUE_VALUE.remove();
            REENTRY_COUNT.remove();
        }
        return isUnLock;
    }

    private String createUniqueValue(){
        String uniqueValue = UNIQUE_VALUE.get();
        if(uniqueValue == null){
            uniqueValue = UUID.randomUUID().toString();
            UNIQUE_VALUE.set(uniqueValue);
        }
        return uniqueValue;
    }

    private boolean isSameLock(String key,String value){
        String v = jedis.get(key);
        return v != null && v.equals(value);
    }

}
