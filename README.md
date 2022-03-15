# gulimall

# rabbitmq处理消息的可靠性保证
## RabbitMQ生产者的消息可靠性的保证

#### 方式1： 异步回调
```java
rabbitTemplate.setConfirmCallback(res -> {
        confirm(id, ack, cause)
    }
)
```


#### 方式2： 持久化
1. 交换机持久化
    在创建交换机时指定Durability为Durable
2. 队列持久化 
   在创建队列时指定Durability为Durable
3. 消息持久化
    Exchange和Queue不会丢失，但是Queue中的消息是会丢失的。消息投递模式（deliveryMode）为2，代表消息
   持久化。

## 消费者消费消息的可靠性保证

#### 消费者应答机制
消费消息的手动应答机制
spring.rabbitmq.listener.simple.acknowledge-mode=manual

#### springboot提供的重试机制
```java
spring:
  rabbitmq:
    listener:
      simple:
        retry:
          enabled: true
          max-attempts: 3 #重试次数
```
在监听消费者的逻辑中需要抛出异常，SpringBoot的重试机制是根据方法中没有捕获异常决定的，
这个重试是重新执行消费者方法，而不是mq重新推送消息。

## MQ中的消息的可靠性保证
不开启持久化的情况下 RabbitMQ 重启之后所有队列和消息都会消失，所以我们创建队列时设置持久化，
发送消息时再设置消息的持久化即可（设置 deliveryMode 为 2 就行了）。一般来说在实际业务中持久化是必须开的。

## 消息的顺序性保证
