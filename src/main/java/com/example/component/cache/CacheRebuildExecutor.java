package com.example.component.cache;

/**
 * 缓存重建数据处理器
 * Author：cedo
 * Date：2020/8/8 22:17
 */
public interface CacheRebuildExecutor<T> {

    /**
     * 读取、组装待重建的数据
     * @return
     */
    T readDataExecute();
}
