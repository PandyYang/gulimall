package com.pandy.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.pandy.common.constant.CartConstant;
import com.pandy.common.to.mq.OrderTo;
import com.pandy.common.to.mq.SeckillOrderTo;
import com.pandy.common.utils.R;
import com.pandy.common.vo.MemberResponseVo;
import com.pandy.gulimall.order.constant.OrderConstant;
import com.pandy.gulimall.order.entity.OrderItemEntity;
import com.pandy.gulimall.order.enume.OrderStatusEnum;
import com.pandy.gulimall.order.feign.CartFeignService;
import com.pandy.gulimall.order.feign.MemberFeignService;
import com.pandy.gulimall.order.feign.ProductFeignService;
import com.pandy.gulimall.order.feign.WmsFeignService;
import com.pandy.gulimall.order.interceptor.LoginUserInterceptor;
import com.pandy.gulimall.order.service.OrderItemService;
import com.pandy.gulimall.order.to.OrderCreateTo;
import com.pandy.gulimall.order.vo.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.order.dao.OrderDao;
import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    OrderDao orderDao;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    WmsFeignService wmsFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
       OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> getAddressFuture = CompletableFuture.runAsync(() -> {
            // ???????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // ????????????????????????????????????
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(currentUserCartItems);
        }, threadPoolExecutor);

        // ??????????????????
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        // ????????????
        String token = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResponseVo.getId(), token, 30, TimeUnit.MINUTES);

        confirmVo.setOrderToken(token);

        CompletableFuture.allOf(getAddressFuture, cartFuture).get();

        return confirmVo;

    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {

        SubmitOrderResponseVo response = new SubmitOrderResponseVo();
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        confirmVoThreadLocal.set(submitVo);
        // ???????????? ?????????????????????????????????????????????
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 0???????????? 1????????????
        String orderToken = submitVo.getOrderToken();
        // ?????????????????????????????????
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result == 0L) {
            return response;
        }else {
            // ?????? ???????????? ???????????? ???????????? ?????????
            OrderCreateTo order = createOrder();
            // ??????
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // ????????????
                saveOrder(order);
                // ????????????
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> collect = order.getOrderItems().stream().map(item -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(collect);
                // ???????????????
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // ?????????
                    response.setOrder(order.getOrder());
                    //??????????????????????????? ??????????????????????????????????????????????????????
                    rabbitTemplate.convertAndSend("order-event-exchange",
                            "order.create.order",order.getOrder());
                    //?????????????????????
                    BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(CartConstant.CART_PREFIX + memberResponseVo.getId());

                    return response;
                } else {
                    response.setCode(3);
                    // ????????????
                    return response;
                }
            } else {
                response.setCode(2);
                return response;
            }
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity order_sn = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return order_sn;
    }

    /**
     * ????????????????????????
     * @param orderEntity
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        //??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        OrderEntity newOrderEntity = this.getById(orderEntity.getId());
        //??????????????????????????????????????????????????????????????????????????????
        if (newOrderEntity.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity updateOrder = new OrderEntity();
            updateOrder.setId(newOrderEntity.getId());
            updateOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updateOrder);

            //????????????????????????????????????????????????????????????????????????????????????
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(newOrderEntity,orderTo);
            rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
        }
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        //TODO
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderTo.getOrderSn());
        entity.setMemberId(orderTo.getMemberId());
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = orderTo.getSeckillPrice().multiply(new BigDecimal(orderTo.getNum()));
        entity.setPayAmount(multiply);
        this.save(entity);

        // TODO ?????????????????????
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(orderTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);

        orderItemEntity.setSkuQuantity(orderTo.getNum());
        orderItemService.save(orderItemEntity);
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        orderDao.insert(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // ???????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity entity = buildOrder(orderSn);

        // ????????????????????????
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // ??????
        computePrice(entity, orderItemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        // 1.?????????????????????
        BigDecimal total = new BigDecimal("0.0");
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            BigDecimal realAmount = orderItemEntity.getRealAmount();
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            intergration.add(orderItemEntity.getIntegrationAmount());
            promotion.add(orderItemEntity.getPromotionAmount());
            gift.add(new BigDecimal(orderItemEntity.getGiftIntegration()));
            growth.add(new BigDecimal(orderItemEntity.getGiftGrowth()));
            total.add(realAmount);
        }
        entity.setTotalAmount(total);
        // ????????????
        entity.setPayAmount(total.add(entity.getFreightAmount()));
        entity.setPromotionAmount(promotion);
        entity.setCouponAmount(coupon);
        entity.setIntegrationAmount(intergration);
        entity.setIntegration(gift.intValue());
        entity.setGrowth(growth.intValue());
    }

    private OrderEntity buildOrder(String orderSn) {

        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();

        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(memberResponseVo.getId());
        // ????????????????????????
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // ????????????????????????
        FareVo fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());

        entity.setFreightAmount(fare.getFare());
        entity.setReceiverCity(fare.getAddress().getCity());
        entity.setReceiverDetailAddress(fare.getAddress().getDetailAddress());
        entity.setReceiverName(fare.getAddress().getName());
        entity.setReceiverPhone(fare.getAddress().getPhone());
        entity.setReceiverPostCode(fare.getAddress().getPostCode());
        entity.setReceiverProvince(fare.getAddress().getProvince());
        entity.setReceiverRegion(fare.getAddress().getRegion());

        // ??????????????????
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        return entity;
    }

    /**
     * ?????????????????????
     * @param
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItemEntityList = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);;
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntityList;
        }
        return null;
    }

    /**
     * ????????????????????????
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // ?????????????????????????????????
        // ???????????? ?????????
        // ?????????spu??????
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());

        // ?????????sku??????
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        // ?????????????????????
        // ????????????
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        // ????????????????????????
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal multiply = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = multiply.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }
}
