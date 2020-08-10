package com.example.component.lock;

public interface DistributeLock {

    boolean lock(String key, long spinTime,long spinTimeInterval, long expireTime);

    boolean unLock(String key);

}
