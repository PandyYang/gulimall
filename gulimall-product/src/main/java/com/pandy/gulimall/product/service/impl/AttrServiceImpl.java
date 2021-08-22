package com.pandy.gulimall.product.service.impl;

import com.pandy.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.pandy.gulimall.product.dao.AttrGroupDao;
import com.pandy.gulimall.product.dao.CategoryDao;
import com.pandy.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.pandy.gulimall.product.entity.AttrGroupEntity;
import com.pandy.gulimall.product.entity.CategoryEntity;
import com.pandy.gulimall.product.vo.AttrResponseVo;
import com.pandy.gulimall.product.vo.AttrVo;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.product.dao.AttrDao;
import com.pandy.gulimall.product.entity.AttrEntity;
import com.pandy.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo) throws InvocationTargetException, IllegalAccessException {
        AttrEntity attrEntity = new AttrEntity();
//        attrEntity.setAttrName(attrVo.getAttrName());
        BeanUtils.copyProperties(attrEntity, attrVo);
        this.save(attrEntity);

        // 保存关联关系
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attrVo.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public PageUtils queryBaseAttr(Map<String, Object> params, Long categoryId) {

        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();

        if (categoryId != 0) {
            queryWrapper.eq("catelog_id", categoryId);
        }

        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((res) -> {
                res.eq("attr_id", key).or().like("attr_name", key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        PageUtils pageUtils = new PageUtils(page);

        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> attr_id1 = records.stream().map((attrEntity) -> {
            AttrResponseVo attrResponseVo = new AttrResponseVo();

            try {
                BeanUtils.copyProperties(attrResponseVo, attrEntity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            AttrAttrgroupRelationEntity attr_id = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attrEntity.getAttrId()));

            if (attr_id != null) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attr_id.getAttrGroupId());
                attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResponseVo.setCatelogName(categoryEntity.getName());
            }

            return attrResponseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attr_id1);
        return pageUtils;
    }
}
