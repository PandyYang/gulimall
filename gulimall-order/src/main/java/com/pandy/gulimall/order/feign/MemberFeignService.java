package com.pandy.gulimall.order.feign;

import com.pandy.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/10/9 23:33
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @RequestMapping("member/memberreceiveaddress/{memberId}/addresses")
    public List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
