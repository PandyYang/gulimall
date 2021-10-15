package com.pandy.gulimall.ware.exception;

/**
 * @Author Pandy
 * @Date 2021/10/15 22:43
 */
public class NoStockException extends RuntimeException {
    private Long skuId;
    public NoStockException(Long skuId) {
        super(skuId + "没有足够的库存了");
    }
}
