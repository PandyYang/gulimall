package com.pandy.gulimall.coupon.dao;

import com.pandy.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:25:52
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
