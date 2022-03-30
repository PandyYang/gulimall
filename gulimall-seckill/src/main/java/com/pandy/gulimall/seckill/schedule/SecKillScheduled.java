package com.pandy.gulimall.seckill.schedule;

import com.pandy.gulimall.seckill.service.SecKillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SecKillScheduled {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private SecKillService secKillService;

    //秒杀商品上架功能的锁
    private final String upload_lock = "seckill:upload:lock";
    /**
     * 定时任务
     * 每天三点上架最近三天的秒杀商品
     */
    @Async
    @Scheduled(cron = "5 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {

        /**
         * 分布式环境下，使用定时任务同时上架秒杀商品的时候，也需要加分布式锁。
         * 但是每个机器上的时间应该是不一样的，这个怎么解决呢？
         * 把redis的秒杀信息存入队列，只要任意一台机器在其秒杀区间内消费掉即可。
         * 即便时间是不一样的，分布式锁只要确保在同一瞬间不会存在多台机器重复消费。
         */
        RLock lock = redissonClient.getLock(upload_lock);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            secKillService.uploadSeckillSkuLatest3Days();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
