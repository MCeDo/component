package com.example.component.cache.string;

import com.example.component.cache.TurboCacheResult;

/**
 * Author：cedo
 * Date：2020/8/9 1:55
 */
public interface StringTurboCacheCommands {

    TurboCacheResult<String> get(String key);

    void set(String key, String value, int expireSeconds);
}
