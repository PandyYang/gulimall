package com.pandy.gulimall.order;

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
        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
        orderReturnReasonEntity.setId(1L);
        orderReturnReasonEntity.setName("test");
        orderReturnReasonEntity.setCreateTime(new Date());
        orderReturnReasonEntity.setSort(1);
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderReturnReasonEntity);
        log.info("消息发生完成");
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
