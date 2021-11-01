package com.pandy.gulimall.product.feign;

import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author Pandy
 * @Date 2021/11/1 22:30
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {

    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId);
}
