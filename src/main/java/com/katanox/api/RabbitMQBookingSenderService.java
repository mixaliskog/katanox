package com.katanox.api;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQBookingSenderService {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Value("${katanox.rabbitmq.exchange}")
    private String exchange;

    @Value("${katanox.rabbitmq.routingkey}")
    private String routingkey;

    public void ObjectRabbitMQSender(Object company) {
        rabbitTemplate.convertAndSend(exchange, routingkey, company);
    }
}
