package com.pandy.gulimall.ware.dao;

import com.pandy.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author Pandy
 * @email yangpandy@gmail.com
 * @date 2021-08-11 00:45:45
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
