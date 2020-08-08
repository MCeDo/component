package com.example.component.cache;

/**
 * 缓存重建处理器
 * Author：cedo
 * Date：2020/8/8 22:17
 */
public interface CacheRebuildExecutor<T> {

    T rebuildCache();
}
