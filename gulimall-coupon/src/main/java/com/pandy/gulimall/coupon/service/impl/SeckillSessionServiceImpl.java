package com.pandy.gulimall.coupon.service.impl;

import com.pandy.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.pandy.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.coupon.dao.SeckillSessionDao;
import com.pandy.gulimall.coupon.entity.SeckillSessionEntity;
import com.pandy.gulimall.coupon.service.SeckillSessionService;

import static sun.misc.PerformanceLogger.getStartTime;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSeckillSessionsIn3Days() {

        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", getStartTime(), getEndTime());
        List<SeckillSessionEntity> seckillSessionEntities = this.list(queryWrapper);
        List<SeckillSessionEntity> list = seckillSessionEntities.stream().map(session -> {
            List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
            session.setRelations(skuRelationEntities);
            return session;
        }).collect(Collectors.toList());

        return list;
    }

    //????????????+2 23:59:59..
    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(2).atTime(LocalTime.MAX);
        String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}
