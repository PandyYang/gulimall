package com.pandy.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:32:27
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 获取用户的收货地址
     * @param memberId
     * @return
     */
    List<MemberReceiveAddressEntity> getAddress(Long memberId);
}

