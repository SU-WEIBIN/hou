package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

// 启动项目前需要先运行一次   创建队列
public class MqInitMain {
    public static void main(String[] args) {


        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel =  connection.createChannel();

            String ExCHANGE_NAME = BiMessageConstant.BI_EXCHANGE_NAME;
            channel.exchangeDeclare(ExCHANGE_NAME,"direct");

            // 创建队列
            String queueName = BiMessageConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName,true,false,false,null    );
            channel.queueBind(queueName,ExCHANGE_NAME,BiMessageConstant.BI_ROUTING_KEY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    return;

    }
}
