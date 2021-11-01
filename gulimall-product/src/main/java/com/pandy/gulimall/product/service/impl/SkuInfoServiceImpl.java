package com.pandy.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.pandy.common.utils.R;
import com.pandy.gulimall.product.entity.SkuImagesEntity;
import com.pandy.gulimall.product.entity.SpuInfoDescEntity;
import com.pandy.gulimall.product.feign.SeckillFeignService;
import com.pandy.gulimall.product.service.*;
import com.pandy.gulimall.product.vo.ItemSaleAttrVo;
import com.pandy.gulimall.product.vo.SeckillInfoVo;
import com.pandy.gulimall.product.vo.SkuItemVo;
import com.pandy.gulimall.product.vo.SpuItemAttrGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.product.dao.SkuInfoDao;
import com.pandy.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService descService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {

        this.baseMapper.insert(skuInfoEntity);

    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrVosFuture = infoFuture.thenAcceptAsync((res) -> {
            List<ItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBuSpuId(res.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> spuInfoDescFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity spuInfoDesc = descService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        }, executor);

        CompletableFuture<Void> attrGroupsFuture = infoFuture.thenAcceptAsync(res -> {
            List<SpuItemAttrGroup> attrGroups = attrGroupService.getAttrGroupWithAttrBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroups);
        }, executor);

        CompletableFuture<Void> imagesFeture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, executor);

        CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
            R seckillSkuInfo = seckillFeignService.getSeckillSkuInfo(skuId);
            if (seckillSkuInfo.getCode() == 0) {
                SeckillInfoVo data = seckillSkuInfo.getData(new TypeReference<SeckillInfoVo>() {
                });
                skuItemVo.setSeckillInfoVo(data);
            }
        }, executor);

        CompletableFuture.allOf(saleAttrVosFuture, spuInfoDescFuture,
                attrGroupsFuture, imagesFeture, seckillFuture).get();

        return skuItemVo;
    }

}
