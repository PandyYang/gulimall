package com.pandy.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.ware.entity.WareSkuEntity;

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
}

