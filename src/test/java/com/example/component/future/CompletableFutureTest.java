package com.example.component.future;

import java.util.concurrent.CompletableFuture;

/**
 * Author：cedo
 * Date：2020/8/9 14:29
 */
public class CompletableFutureTest {

    public static void main(String[] args) throws InterruptedException {
        CompletableFuture future = CompletableFuture.runAsync(
                () -> {
                    System.out.println("异步调用测试, current Thread: " + Thread.currentThread());
                }
        );
        Thread.sleep(100);
        System.out.println("main thread: " + Thread.currentThread());
    }
}
