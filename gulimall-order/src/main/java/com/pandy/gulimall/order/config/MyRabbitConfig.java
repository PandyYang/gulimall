package com.pandy.gulimall.order.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Author Pandy
 * @Date 2021/9/27 10:15
 */
@Configuration
@Slf4j
public class MyRabbitConfig {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制rabbitTemplate
     */
    @PostConstruct // MyRabbitConfig创建完成之后执行这个方法
    public void initRabbitTemplate() {

        /**
         * 消息成功
         * confirm监听，当消息成功发送到交换机 ack = true, 没有发送到交换机 ack = false
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 当前消息的唯一关联数据（是消息的唯一id）
             * @param ack 消息是否成功收到
             * @param cause 失败原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("Confirm..correlationData[ " + correlationData +"]==>ack==> " +ack + " cause" + cause);
                if (!ack) {
                    System.out.println("消息发送至交换机失败，生产者生产消息失败！记录日志，发送邮件通知，落库定时任务扫描重发");
                    log.error("消息发送失败: " + correlationData + "失败原因: " + cause);
                }
            }
        });

        /**
         * 消息抵达队列的确认回调
         * 当消息成功发送到交换机但是没有路由到队列触发此监听
         */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就触发这个失败回调。
             * @param message 投递失败的消息详细信息
             * @param replyCode 回复状态吗
             * @param replyText 回复内容
             * @param exchange 发送消息所用交换机
             * @param routingKey 发送消息所用路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println(message + "消息未送达指定队列时回调");
            }
        });
    }
}
