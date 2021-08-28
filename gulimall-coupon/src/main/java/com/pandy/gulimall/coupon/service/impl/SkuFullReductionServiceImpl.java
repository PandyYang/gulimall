package com.pandy.gulimall.coupon.service.impl;

import com.pandy.common.to.MemberPrice;
import com.pandy.common.to.SkuReductionTo;
import com.pandy.gulimall.coupon.entity.MemberPriceEntity;
import com.pandy.gulimall.coupon.entity.SkuLadderEntity;
import com.pandy.gulimall.coupon.service.MemberPriceService;
import com.pandy.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.coupon.dao.SkuFullReductionDao;
import com.pandy.gulimall.coupon.entity.SkuFullReductionEntity;
import com.pandy.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
//        skuLadderEntity.setPrice(setPrice);
        skuLadderService.save(skuLadderEntity);

        // 满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        this.save(skuFullReductionEntity);

        // 会员价格
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();

        List<MemberPriceEntity> collect = memberPrice.stream().map(res -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(res.getId());
            memberPriceEntity.setMemberPrice(res.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}
