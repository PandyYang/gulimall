package com.pandy.gulimall.ware.service.impl;

import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.gulimall.ware.exception.NoStockException;
import com.pandy.gulimall.ware.vo.LockStockResult;
import com.pandy.gulimall.ware.vo.OrderItemVo;
import com.pandy.gulimall.ware.vo.WareSkuLockVo;
import lombok.Data;
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

import com.pandy.gulimall.ware.dao.WareSkuDao;
import com.pandy.gulimall.ware.entity.WareSkuEntity;
import com.pandy.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkusStock(List<Long> skuIds) {
        return skuIds.stream().map(id -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();

            // 查询当前sku的总库存量
            stockVo.setSkuId(id);
            // 这里库存可能为null 要避免空指针异常
            stockVo.setHasStock(baseMapper.getSkuStock(id)==null?false:true);
            return stockVo;
        }).collect(Collectors.toList());
    }

    /**
     * 为某订单锁库存
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLocks();
        List<SkuWareHashStock> collect = locks.stream().map(item -> {
            SkuWareHashStock stock = new SkuWareHashStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            List<Long> wareIds = wareSkuDao.listWareIdHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        Boolean allLock = true;
        for (SkuWareHashStock hashStock : collect) {
            Boolean skuStock = false;
            Long skuId = hashStock.getSkuId();
            List<Long> wareIds = hashStock.getWareId();
            if (wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功返回1
                Long aLong = wareSkuDao.lockSkuStock(skuId, wareId, hashStock.getNum());
                if (aLong == 1) {
                    skuStock = true;
                    break;
                } else {

                }
            }
            if (skuStock == false) {
                throw new NoStockException(skuId);
            }
        }
        // 锁定成功
        return true;
    }

    @Data
    class SkuWareHashStock {
        private Long SkuId;
        private List<Long> wareId;
        private Integer num;
    }

}
