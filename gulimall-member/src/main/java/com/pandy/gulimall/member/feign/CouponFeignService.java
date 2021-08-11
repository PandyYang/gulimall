package com.pandy.gulimall.member.feign;

import com.pandy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author Pandy
 * @Date 2021/8/12 1:13
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/coupon/member/list")
    public R membercoupons();

}
