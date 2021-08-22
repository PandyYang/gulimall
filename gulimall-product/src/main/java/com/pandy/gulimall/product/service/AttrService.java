package com.pandy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.product.entity.AttrEntity;
import com.pandy.gulimall.product.vo.AttrVo;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * 商品属性
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:58
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVo attrVo) throws InvocationTargetException, IllegalAccessException;

    PageUtils queryBaseAttr(Map<String, Object> params, Long categoryId);
}

