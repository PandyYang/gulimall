package com.pandy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.product.entity.SkuInfoEntity;
import com.pandy.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku??Ϣ
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 22:15:36
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    SkuItemVo item(Long skuId);
}

