package com.pandy.gulimall.order.feign;

import com.pandy.common.utils.R;
import com.pandy.gulimall.order.vo.FareVo;
import com.pandy.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Pandy
 * @Date 2021/10/12 22:46
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {
    @RequestMapping("/ware/wareinfo/fare/{addrId}")
    public FareVo getFare(@PathVariable("addrId") Long addrId);

    @PostMapping("ware/waresku/lock")
    public R orderLockStock(WareSkuLockVo vo);
}
