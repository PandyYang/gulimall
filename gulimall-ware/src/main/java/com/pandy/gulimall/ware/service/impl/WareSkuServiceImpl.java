package com.pandy.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.pandy.common.to.es.SkuHasStockVo;
import com.pandy.common.to.mq.OrderTo;
import com.pandy.common.to.mq.StockDetailTo;
import com.pandy.common.to.mq.StockLockedTo;
import com.pandy.common.utils.R;
import com.pandy.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.pandy.gulimall.ware.entity.WareOrderTaskEntity;
import com.pandy.gulimall.ware.enume.OrderStatusEnum;
import com.pandy.gulimall.ware.enume.WareTaskStatusEnum;
import com.pandy.gulimall.ware.exception.NoStockException;
import com.pandy.gulimall.ware.feign.OrderFeignService;
import com.pandy.gulimall.ware.service.WareOrderTaskDetailService;
import com.pandy.gulimall.ware.service.WareOrderTaskService;
import com.pandy.gulimall.ware.vo.LockStockResult;
import com.pandy.gulimall.ware.vo.OrderItemVo;
import com.pandy.gulimall.ware.vo.OrderVo;
import com.pandy.gulimall.ware.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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


@RabbitListener
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    WareSkuDao wareSkuDao;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    OrderFeignService orderFeignService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {

        StockDetailTo detail = to.getDetailTo();
        Long skuId = detail.getSkuId();
        Long detailId = detail.getId();
        // 没有: 库存锁定失败 无需解锁
        // 有 解锁
                // 没有订单必须解锁
                // 有订单
                    // 订单状态 : 已取消 解锁
                                // 没取消 不能解锁
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if (byId != null) {
            // 解锁
            Long id = to.getId(); // 库存工作单的id
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(id);
            String orderSn = taskEntity.getOrderSn(); // 根据订单号 查状态
            R orderStatus = orderFeignService.getOrderStatus(orderSn);
            if (orderStatus.getCode() == 0) {
                OrderVo data = orderStatus.getData(new TypeReference<OrderVo>() {

                });
                if (data.getStatus() == 4 || data == null) {
                    // 订单取消才能进行解锁
                    if (byId.getLockStatus() == 1) {
                        unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(),
                                detailId);
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    }
                } else {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                }
            }
        } else {

        }
    }

    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        //数据库中解锁库存数据
        baseMapper.unlockStock(skuId, wareId, num);
        //更新库存工作单详情的状态
        WareOrderTaskDetailEntity detail = WareOrderTaskDetailEntity.builder()
                .id(taskDetailId)
                .lockStatus(2).build();
        wareOrderTaskDetailService.updateById(detail);
    }

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

        // 保存库存工作单
        WareOrderTaskEntity wareOrderTaskEntity = new WareOrderTaskEntity();
        wareOrderTaskEntity.setOrderSn(vo.getOrderSn());
        wareOrderTaskService.save(wareOrderTaskEntity);

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
            // 锁定成功 将当前商品锁定几件工作单发送mq
            // 锁定失败 前面保存的工作单信息就回滚了 发出去的消息 即使是要解锁的记录
            // 由于数据库查不到记录 就不用
            for (Long wareId : wareIds) {
                // 成功返回1
                Long aLong = wareSkuDao.lockSkuStock(skuId, wareId, hashStock.getNum());
                if (aLong == 1) {
                    skuStock = true;
                    WareOrderTaskDetailEntity wareOrderTaskDetailEntity = new WareOrderTaskDetailEntity(
                            null,skuId,"",hashStock.getNum(),wareOrderTaskEntity.getId(),wareId, 1
                    );
                    wareOrderTaskDetailService.save(wareOrderTaskDetailEntity);
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(wareOrderTaskEntity.getId());
                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(wareOrderTaskEntity, stockDetailTo);
                    stockLockedTo.setDetailTo(stockDetailTo);
                    rabbitTemplate.convertAndSend("stock-event-exchange",
                            "stock.locked", stockLockedTo);
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

    @Override
    public void unlock(StockLockedTo stockLockedTo) {
        StockDetailTo detailTo = stockLockedTo.getDetailTo();
        WareOrderTaskDetailEntity detailEntity = wareOrderTaskDetailService.getById(detailTo.getId());
        //1.如果工作单详情不为空，说明该库存锁定成功
        if (detailEntity != null) {
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(stockLockedTo.getId());
            R r = orderFeignService.infoByOrderSn(taskEntity.getOrderSn());
            if (r.getCode() == 0) {
                OrderTo order = r.getData("order", new TypeReference<OrderTo>() {
                });
                //没有这个订单||订单状态已经取消 解锁库存
                if (order == null||order.getStatus()== OrderStatusEnum.CANCLED.getCode()) {
                    //为保证幂等性，只有当工作单详情处于被锁定的情况下才进行解锁
                    if (detailEntity.getLockStatus()== WareTaskStatusEnum.Locked.getCode()){
                        unlockStock(detailTo.getSkuId(), detailTo.getSkuNum().longValue(), detailTo.getWareId().intValue(), detailEntity.getId());
                    }
                }
            }else {
                throw new RuntimeException("远程调用订单服务失败");
            }
        }else {
            //无需解锁
        }
    }

    @Override
    public void unlock(OrderTo orderTo) {
        //为防止重复解锁，需要重新查询工作单
        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity taskEntity = wareOrderTaskService.getBaseMapper().selectOne((new QueryWrapper<WareOrderTaskEntity>().eq("order_sn", orderSn)));
        //查询出当前订单相关的且处于锁定状态的工作单详情
        List<WareOrderTaskDetailEntity> lockDetails = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()).eq("lock_status", WareTaskStatusEnum.Locked.getCode()));
        for (WareOrderTaskDetailEntity lockDetail : lockDetails) {
            unlockStock(lockDetail.getSkuId(),lockDetail.getSkuNum().longValue(),lockDetail.getWareId().intValue(),lockDetail.getId());
        }
    }

    @Data
    class SkuWareHashStock {
        private Long SkuId;
        private List<Long> wareId;
        private Integer num;
    }

}
