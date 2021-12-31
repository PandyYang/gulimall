package com.pandy.gulimall.order;

import com.pandy.gulimall.order.entity.OrderEntity;
import com.pandy.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage() {

        // 发送不同类型的消息到不同的rabbitHandler
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
                orderReturnReasonEntity.setId(1L);
                orderReturnReasonEntity.setName("test");
                orderReturnReasonEntity.setCreateTime(new Date());
                orderReturnReasonEntity.setSort(1);
                // 指定不同的交换机和路由
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderReturnReasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
            }
        }

        log.info("消息发送完成");
    }

    @Test
    public void createExchange() {

        DirectExchange directExchange = new DirectExchange("hello-java-exchange"
        ,true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange创建成功");
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("队列[{}]创建成功", "hello-java-exchange");
    }

    @Test
    public void createBinding() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("binding创建成功");
    }
}
