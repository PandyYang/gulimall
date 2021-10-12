package com.pandy.gulimall.order.to;

import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Pandy
 * @Date 2021/10/12 22:39
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    // 应付价格
    private BigDecimal payPrice;

    // 优惠
    private BigDecimal fare;

}
