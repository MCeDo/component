package com.example.component.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 双涡轮缓存读取结果Data
 * Author：cedo
 * Date：2020/8/8 22:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TurboCacheResult<T> {

    /**
     * 缓存数据信息
     */
    private T data;

    private boolean expire;

    /**
     * 当前版本生效缓存key信息
     */
    private TwinTurboKey twinTurboKey;

}
