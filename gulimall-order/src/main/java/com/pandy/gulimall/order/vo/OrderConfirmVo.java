package com.pandy.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author Pandy
 * @Date 2021/10/9 23:02
 */

public class OrderConfirmVo {

    // 收货地址
    List<MemberAddressVo> memberAddressVos;

    // 所有选中项
    List<OrderItemVo> items;

    // 发票记录

    // 优惠券信息
    Integer integration;

    // 订单总额
    BigDecimal total;

    // 应付价格
    BigDecimal payPrice;

    public List<MemberAddressVo> getMemberAddressVos() {
        return memberAddressVos;
    }

    public void setMemberAddressVos(List<MemberAddressVo> memberAddressVos) {
        this.memberAddressVos = memberAddressVos;
    }

    public List<OrderItemVo> getItems() {
        return items;
    }

    public void setItems(List<OrderItemVo> items) {
        this.items = items;
    }

    public Integer getIntegration() {
        return integration;
    }

    public void setIntegration(Integer integration) {
        this.integration = integration;
    }

    @Setter @Getter
    String orderToken;

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPayPrice() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null) {
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    @Getter @Setter
    Map<Long,Boolean> stocks;

    public void setPayPrice(BigDecimal payPrice) {
        this.payPrice = payPrice;
    }
}
