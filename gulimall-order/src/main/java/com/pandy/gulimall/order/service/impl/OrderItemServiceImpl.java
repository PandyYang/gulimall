package com.pandy.gulimall.order.service.impl;

import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pandy.common.utils.PageUtils;
import com.pandy.common.utils.Query;

import com.pandy.gulimall.order.dao.OrderItemDao;
import com.pandy.gulimall.order.entity.OrderItemEntity;
import com.pandy.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues 声明需要监听的所有队列
     * Message 原生消息详细信息 头 + 体
     * 发送的消息类型 OrderReturnReasonEntity
     * Channel channel 当前传输数据的通道
     *
     * Queue 可以有很多人同时监听
     *      1。多人接收消息只会有一个收到
     *      2。只有当前消息处理完成 才会接收下一个消息
     *
     * RabbitListener 可以在类（可以接收哪些队列的消息） 方法上
     * RabbitHandler 只能在方法上 两者结合可以接收不同类型的消息 重载区分不同的消息
     * @param message
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        // 消息体
        byte[] body = message.getBody();
        MessageProperties messageProperties = message.getMessageProperties();
        TimeUnit.SECONDS.sleep(3);
        System.out.println("接收到新消息" + message + "-----》 内容：" + content);
    }


    @RabbitHandler
    public void receiveMessage(OrderEntity content) throws InterruptedException {
        System.out.println("接收到新消息" + content);
    }


}
