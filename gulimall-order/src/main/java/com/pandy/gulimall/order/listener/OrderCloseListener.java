package com.pandy.gulimall.order.listener;

import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = {"order.release.order.queue"})
@Slf4j
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    /**
     *
     * @param orderEntity
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单" + orderEntity.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e){
            log.error("签收失败", e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException exception) {
                log.error("拒签失败", exception);
            }
//            channel.basicReject(deliveryTag,true);
        }
    }
}
