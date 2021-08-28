package com.pandy.gulimall.product.feign;

import com.pandy.common.to.SkuReductionTo;
import com.pandy.common.to.SpuBoundTo;
import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Author Pandy
 * @Date 2021/8/28 8:00
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
