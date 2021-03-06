package com.pandy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.product.entity.SpuInfoDescEntity;
import com.pandy.gulimall.product.entity.SpuInfoEntity;
import com.pandy.gulimall.product.vo.SpuSaveVo;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * spu信息
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:57
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo) throws InvocationTargetException, IllegalAccessException;

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

