package com.pandy.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.pandy.gulimall.product.service.CategoryBrandRelationService;
import com.pandy.gulimall.product.vo.Catalog3Vo;
import com.pandy.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * 空结果缓存 解决缓存穿透
     * 设置过期时间 解决缓存雪崩
     * 加锁 解决缓存击穿
     * @return
     */

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException {

        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDBWithRedisLock();

            return catelogJsonFromDB;
        }
        Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return stringListMap;
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {
        synchronized (this) {
            String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
            if (!StringUtils.isEmpty(catalogJson)) {
                Map<String, List<Catelog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
                return stringListMap;
            }
            return getDataFromDB();
        }
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() throws InterruptedException {

            //1.抢占分布式锁  原子命令

        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111", 300, TimeUnit.SECONDS);
        if (lock) {

            // 2.设置过期时间加锁成功 获取数据释放锁 [分布式下必须是Lua脚本删锁,不然会因为业务处理时间、网络延迟等等引起数据还没返回锁过期或者返回的过程中过期 然后把别人的锁删了]
            Map<String, List<Catelog2Vo>> data;
            try {
                data = getDataFromDB();
            } finally {
//			stringRedisTemplate.delete("lock");
                String lockValue = stringRedisTemplate.opsForValue().get("lock");

                // 删除也必须是原子操作 Lua脚本操作 删除成功返回1 否则返回0
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 原子删锁
                stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        } else {
            Thread.sleep(200);
            // 加锁失败
            return getCatelogJsonFromDBWithRedisLock();
        }


    }

    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        List<CategoryEntity> entityList = baseMapper.selectList(null);
        // 查询所有一级分类
        List<CategoryEntity> level1 = getCategoryEntities(entityList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 拿到每一个一级分类 然后查询他们的二级分类
            List<CategoryEntity> entities = getCategoryEntities(entityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (entities != null) {
                catelog2Vos = entities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), l2.getName(), l2.getCatId().toString(), null);
                    // 找当前二级分类的三级分类
                    List<CategoryEntity> level3 = getCategoryEntities(entityList, l2.getCatId());
                    // 三级分类有数据的情况下
                    if (level3 != null) {
                        List<Catalog3Vo> catalog3Vos = level3.stream().map(l3 -> new Catalog3Vo(l3.getCatId().toString(), l3.getName(), l2.getCatId().toString())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(catalog3Vos);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catalogJson", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * 第一次查询的所有 CategoryEntity 然后根据 parent_cid去这里找
     */
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> entityList, Long parent_cid) {

        return entityList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
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
