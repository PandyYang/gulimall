package com.pandy.gulimall.product.dao;

import com.pandy.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-10 23:46:58
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
