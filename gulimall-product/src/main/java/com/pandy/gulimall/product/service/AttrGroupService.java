package com.pandy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.product.entity.AttrGroupEntity;
import com.pandy.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.pandy.gulimall.product.vo.SpuItemAttrGroup;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:58
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId);

    List<SpuItemAttrGroup> getAttrGroupWithAttrBySpuId(Long spuId, Long catalogId);
}

