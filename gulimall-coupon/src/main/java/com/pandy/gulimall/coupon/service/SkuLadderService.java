package com.pandy.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.coupon.entity.SkuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:25:53
 */
public interface SkuLadderService extends IService<SkuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

