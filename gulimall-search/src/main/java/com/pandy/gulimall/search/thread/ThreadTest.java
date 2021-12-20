package com.pandy.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果：" + i);
            return i;
        }, executor).whenComplete((res, exception) -> {
            // 虽然能得到异常信息 但是没发修改返回数据
            System.out.println("异步任务完成了...结果是：" + res + "异常是：" + exception);
        }).exceptionally(throwable -> {
            // 感知异常同时返回默认值
            return 10;
        });
        
        Integer integer = future.get();

        System.out.println("main...end..." + integer);
    }
}
