package com.pandy.gulimall.product.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 成直积分、购物积分
 */
@Data
public class Bounds {

    private BigDecimal buyBounds;
    private BigDecimal growBounds;

}
