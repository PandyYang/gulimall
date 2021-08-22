package com.pandy.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pandy.common.utils.PageUtils;
import com.pandy.gulimall.product.entity.CategoryBrandRelationEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:58
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetails(CategoryBrandRelationEntity categoryBrandRelation);

    // 同步更新其他关联表中的数据
    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);
}

