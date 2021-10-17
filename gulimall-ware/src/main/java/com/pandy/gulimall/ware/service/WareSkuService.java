package com.pandy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.common.to.mq.OrderTo;
import com.pandy.common.to.mq.StockLockedTo;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.ware.entity.WareSkuEntity;
import com.pandy.gulimall.ware.vo.LockStockResult;
import com.pandy.gulimall.ware.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:45:45
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockVo> getSkusStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);


    void unlock(StockLockedTo stockLockedTo);

    void unlock(OrderTo stockLockedTo);
}

