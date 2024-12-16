package com.yupi.springbootinit.bizmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    //sendMessage
    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(BiMessageConstant.BI_EXCHANGE_NAME,BiMessageConstant.BI_ROUTING_KEY,message);
    }
    //
}
