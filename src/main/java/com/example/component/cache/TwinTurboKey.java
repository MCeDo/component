package com.example.component.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双版本缓存状态信息缓存KEY
 * Author：cedo
 * Date：2020/8/8 16:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwinTurboKey {

    /**
     * 逻辑缓存过期时间
     */
    private long expireSeconds;

    /**
     * 版本缓存生效中的key
     */
    private String curKey;

    /**
     * 最后更新时间
     */
    private long updateTime;

    /**
     * 缓存更新异常次数
     */
    private int cacheUpdateErrTimes;

    /**
     * 当前版本缓存是否过期
     * @return
     */
    public boolean isExpire() {
        return (System.currentTimeMillis() - updateTime) > expireSeconds * 1000;
    }
}
