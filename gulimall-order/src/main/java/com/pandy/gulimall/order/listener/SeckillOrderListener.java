package com.pandy.gulimall.order.listener;

import com.pandy.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import com.pandy.common.to.mq.SeckillOrderTo;
import com.pandy.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class SeckillOrderListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void createOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("准备创建秒杀单的详细信息。。。");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicReject(deliveryTag,true);
        }
    }
}
