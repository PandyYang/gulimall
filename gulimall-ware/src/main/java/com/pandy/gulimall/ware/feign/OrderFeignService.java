package com.pandy.gulimall.ware.feign;

import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @RequestMapping("order/order/infoByOrderSn/{OrderSn}")
    R infoByOrderSn(@PathVariable("OrderSn") String OrderSn);

    @GetMapping("order/order/status/{orderSn}")
    public R getOrderStatus(@PathVariable String orderSn);
}
