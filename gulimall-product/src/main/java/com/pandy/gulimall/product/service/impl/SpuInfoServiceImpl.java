package com.pandy.gulimall.product.service.impl;

import com.pandy.common.constant.ProductConstant;
import com.pandy.common.to.SkuReductionTo;
import com.pandy.common.to.SpuBoundTo;
import com.pandy.common.to.es.SkuEsModel;
import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.common.utils.R;
import com.pandy.gulimall.product.entity.*;
import com.pandy.gulimall.product.feign.CouponFeignService;
import com.pandy.gulimall.product.feign.SearchFeignService;
import com.pandy.gulimall.product.feign.WareFeignService;
import com.pandy.gulimall.product.service.*;
import com.pandy.gulimall.product.vo.*;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) throws InvocationTargetException, IllegalAccessException {

        // 保存spu基本信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoEntity, vo);
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setCreateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 保存spu的描述图片
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDescript(spuInfoDescEntity);

        // 保存spu图片集
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(collect);
        // 保存spu积分信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImage = "";

                for (Images img : item.getImages()) {
                    if (img.getDefaultImg() == 1) {
                        defaultImage = img.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());

                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(res -> res.getImgUrl() != null).collect(Collectors.toList());

                skuImagesService.saveBatch(imagesEntities);

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> collect1 = attr.stream().map(res -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    try {
                        BeanUtils.copyProperties(skuSaleAttrValueEntity, res);
                        skuSaleAttrValueEntity.setSkuId(skuId);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(collect1);

                Bounds bounds = vo.getBounds();
                SpuBoundTo spuBoundTo = new SpuBoundTo();
                try {
                    BeanUtils.copyProperties(spuBoundTo, bounds);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                spuBoundTo.setSpuId(spuInfoEntity.getId());
                R r = couponFeignService.saveSpuBounds(spuBoundTo);
                if (r.getCode() != 0) {
                    log.error("远程保存spu积分信息失败");
                }

                SkuReductionTo skuReductionTo = new SkuReductionTo();

                try {
                    BeanUtils.copyProperties(skuReductionTo, item);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                skuReductionTo.setSkuId(skuId);
                couponFeignService.saveSkuReduction(skuReductionTo);

            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public void up(Long spuId) {

        // 组装需要的数据
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //TODO 查询当前的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);

        List<Long> ids = baseAttrs.stream().map(attr -> {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        List<Long> attrIds = attrService.selectSearchAttrs(ids);

        Set<Long> idSet = new HashSet<>(attrIds);

        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream().filter(
                item -> {
                    return idSet.contains(item.getAttrId());
                }
        ).map(res -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            try {
                BeanUtils.copyProperties(attrs1, res);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            return attrs1;
        }).collect(Collectors.toList());

        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            try {
                BeanUtils.copyProperties(skuEsModel, sku);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            //TODO  发送远程调用 库存系统是否存在库存
            Map<Long, Boolean> stockMap = null;
            try {
                R<List<SkuHasStockVo>> skusStock = wareFeignService.getSkusStock(skuIdList);

                stockMap = skusStock.getData().stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId,
                        SkuHasStockVo::getHasStock));

            }catch (Exception e) {
                log.error("库存服务器异常:{}", e);
            }

            if (stockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(stockMap.get(sku.getSkuId()));
            }

            //TODO  热度评分 0
            skuEsModel.setHotScore(0L);
            //TODO  品牌和分类的名字信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        // 将数据发送给es
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            //TODO 远程调用成功
          baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            // 远程调用失败 接口幂等性 重复上架
        }

    }


}
