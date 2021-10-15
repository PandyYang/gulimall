package com.pandy.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
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
import org.springframework.beans.factory.annotation.Autowired;
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
            // 远程查询所有的收货地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(memberResponseVo.getId());
            confirmVo.setMemberAddressVos(address);
        }, threadPoolExecutor);

        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            // 远程查询所有选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItems(currentUserCartItems);
        }, threadPoolExecutor);

        // 查询用户积分
        Integer integration = memberResponseVo.getIntegration();
        confirmVo.setIntegration(integration);

        // 防重令牌
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
        // 验证令牌 令牌的对比和删除必须保证原子性
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        // 0校验失败 1删除成功
        String orderToken = submitVo.getOrderToken();
        // 原子验证令牌和删除令牌
        Long result = (Long) redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);
        if (result == 0L) {
            return response;
        }else {
            // 下单 创建订单 验证令牌 验证价格 锁库存
            OrderCreateTo order = createOrder();
            // 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = submitVo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 保存订单
                saveOrder(order);
                // 锁定库存
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
                // 远程锁库存
                R r = wmsFeignService.orderLockStock(wareSkuLockVo);
                if (r.getCode() == 0) {
                    // 锁成功
                    response.setOrder(order.getOrder());
                    return response;
                } else {
                    response.setCode(3);
                    // 锁定失败
                    return response;
                }
            } else {
                response.setCode(2);
                return response;
            }
        }
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
        // 生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity entity = buildOrder(orderSn);

        // 获取所有的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        // 验价
        computePrice(entity, orderItemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity entity, List<OrderItemEntity> orderItemEntities) {
        // 1.订单价格相关的
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
        // 应付总额
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
        // 获取收货地址信息
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        // 远程查询运费信息
        FareVo fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());

        entity.setFreightAmount(fare.getFare());
        entity.setReceiverCity(fare.getAddress().getCity());
        entity.setReceiverDetailAddress(fare.getAddress().getDetailAddress());
        entity.setReceiverName(fare.getAddress().getName());
        entity.setReceiverPhone(fare.getAddress().getPhone());
        entity.setReceiverPostCode(fare.getAddress().getPostCode());
        entity.setReceiverProvince(fare.getAddress().getProvince());
        entity.setReceiverRegion(fare.getAddress().getRegion());

        // 设置状态信息
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        return entity;
    }

    /**
     * 构建所有订单项
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
     * 构建每一个订单项
     * @param cartItem
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 构建每一个需要哪些数据
        // 订单信息 订单号
        // 商品的spu信息
        Long skuId = cartItem.getSkuId();
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo data = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCatalogId());

        // 商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(cartItem.getCount());
        // 商品的优惠信息
        // 积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());
        // 订单项的价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        BigDecimal multiply = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = multiply.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }
}
