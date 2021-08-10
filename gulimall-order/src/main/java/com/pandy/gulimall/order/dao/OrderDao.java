package com.pandy.gulimall.order.dao;

import com.pandy.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:40:52
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
