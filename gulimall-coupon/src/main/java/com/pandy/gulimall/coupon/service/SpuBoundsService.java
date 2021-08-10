package com.pandy.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.coupon.entity.SpuBoundsEntity;

import java.util.Map;

/**
 * 商品spu积分设置
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:25:53
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

