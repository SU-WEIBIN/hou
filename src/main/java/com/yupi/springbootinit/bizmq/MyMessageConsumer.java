package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MyMessageConsumer {

    @SneakyThrows  //忽略异常
    @RabbitListener(queues = {},ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channer, @Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag){
        log.info("receiveMessage message={}", message);
        channer.basicAck(deliveryTag,false);
    }
}
