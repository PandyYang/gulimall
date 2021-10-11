package com.pandy.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.vo.OrderConfirmVo;
import com.pandy.gulimall.order.vo.OrderSubmitVo;
import com.pandy.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:40:52
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);
}

