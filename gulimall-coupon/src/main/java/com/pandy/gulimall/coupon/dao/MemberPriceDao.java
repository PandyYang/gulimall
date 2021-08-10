package com.pandy.gulimall.coupon.dao;

import com.pandy.gulimall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:25:52
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
