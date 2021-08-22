package com.pandy.gulimall.product.service.impl;

import com.pandy.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.product.dao.CategoryDao;
import com.pandy.gulimall.product.entity.CategoryEntity;
import com.pandy.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1,查询所有分类 2.组装树形结构
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 找出所有的1级分类
        List<CategoryEntity> collect = entities.stream()
                .filter(res -> res.getParentCid() == 0)
                .map((menu) -> {
             menu.setChildren(getChildrens(menu, entities));
             return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return collect;
    }

    @Override
    public void removeMenuByIds(List<Long> catIds) {
        //TODO
        baseMapper.deleteBatchIds(catIds);
    }

    // 找到catelogId的完整路径
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = this.findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> listPath) {
        listPath.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), listPath);
        }
        return listPath;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream()
                .filter(item -> Objects.equals(item.getParentCid(), root.getCatId()))
                .map(menu -> { menu.setChildren(getChildrens(menu, all));
            return menu;
        }).sorted((res1, res2) -> {
            return (res1.getSort() == null ? 0 : res1.getSort()) - (res2.getSort() == null ? 0 : res2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}
