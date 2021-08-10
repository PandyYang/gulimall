package com.pandy.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.coupon.entity.MemberPriceEntity;

import java.util.Map;

/**
 * 商品会员价格
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:25:52
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

