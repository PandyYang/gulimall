package com.pandy.gulimall.order.service.impl;

import com.pandy.common.vo.MemberResponseVo;
import com.pandy.gulimall.order.constant.OrderConstant;
import com.pandy.gulimall.order.feign.CartFeignService;
import com.pandy.gulimall.order.feign.MemberFeignService;
import com.pandy.gulimall.order.interceptor.LoginUserInterceptor;
import com.pandy.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.order.dao.OrderDao;
import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    RedisTemplate redisTemplate;

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
        }
//        Object redisToken = redisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId());
//        if (orderToken != null && orderToken.equals(redisToken)) {
//            // 验证通过
//
//        } else {
//            // 不通过
//        }

        return response;
    }
}
