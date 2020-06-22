package com.everjiankang.dependency.springboot;

import java.util.Map;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.everjiankang.dependency.model.Order;

@Component
public class RabbitmqSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * publisher-confirms,实现一个监听器用于监听broker端给我们返回的确认请求：
     * RabbitTemplate.ConfirmCallback
     * 
     * publisher-returns，保证消息对broker端是可达的，如果出现路由键不可达的情况，
     *     则使用监听器对不可达的消息进行后续的处理，保证消息的路由成功：
     * RabbitTemplate.ReturnCallback
     * 
     * 注意：在发送消息的时候对template配置mandatory=true保证ReturnCallback监听有效
     * 生产端还可以配置其他属性，比如发送重试，超时时间、次数、间隔等。
     */
    final ConfirmCallback confirmCallback = new RabbitTemplate.ConfirmCallback() {
        @Override
        public void confirm(CorrelationData correlationData, boolean ack, String cause) {
            System.err.println("confirm correlationData:" + correlationData);
            System.err.println("confirm ack:" + ack);
            if(!ack) {
                System.err.println("异常处理。。。");
            }
        }
    };
    
    final ReturnCallback returnCallback = new RabbitTemplate.ReturnCallback() {
        
        @Override
        public void returnedMessage(org.springframework.amqp.core.Message message, int replyCode, String replyText,
                String exchange, String routingKey) {
            System.err.println("return exchange:" + exchange);
            System.err.println("return routingKey:" + routingKey);
            System.err.println("return replyCode:" + replyCode);
            System.err.println("return replyText:" + replyText);
        }
    };
    
    public void send(Object message, Map<String, Object> properties) throws Exception {
        MessageHeaders mhs = new MessageHeaders(properties);
        Message msg = MessageBuilder.createMessage(message, mhs);
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        CorrelationData correlationData = new CorrelationData();
        //实际消息的唯一id
        correlationData.setId("dwz123456");//id + 时间戳 （必须是全局唯一的）
        rabbitTemplate.convertAndSend("exchange-1", "spring.abc", msg, correlationData);
    }
    
    public void sendOrder(Order order) throws Exception {
        rabbitTemplate.setConfirmCallback(confirmCallback);
        rabbitTemplate.setReturnCallback(returnCallback);
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId("zheaven123456");//id + 时间戳 （必须是全局唯一的）
        rabbitTemplate.convertAndSend("exchange-2", "springboot.def", order, correlationData);
    }
}