package com.pandy.gulimall.order.feign;

import com.pandy.gulimall.order.vo.FareVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Pandy
 * @Date 2021/10/12 22:46
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {
    @RequestMapping("/ware/wareinfo/fare/{addrId}")
    public FareVo getFare(@PathVariable("addrId") Long addrId);
}
