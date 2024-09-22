package com.katanox.api.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.katanox.api.exception.RabbitMQException;
import com.katanox.api.service.LogWriterService;

@Configuration
public class RabbitMQConfig {

  @Value("${katanox.rabbitmq.queue}")
  String queueName;

  @Value("${katanox.rabbitmq.booking.processing.queue}")
  String bookingProcessingQueue;

  @Value("${katanox.rabbitmq.exchange}")
  String exchange;

  @Value("${katanox.rabbitmq.routingkey}")
  private String routingkey;

  @Bean
  Queue queue() {
    return new Queue(queueName, false);
  }

  @Bean
  DirectExchange exchange() {
    return new DirectExchange(exchange);
  }

  @Bean
  Binding binding(Queue queue, DirectExchange exchange) {
    return BindingBuilder.bind(queue).to(exchange).with(routingkey);
  }

  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }


  @Bean
  public AmqpTemplate rabbitTemplate(final CachingConnectionFactory connectionFactory,
                                     final LogWriterService logWriterService) {
    connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);  // Enables confirms
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

    rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
      if (ack) {
        logWriterService.logStringToConsoleOutput("Message sent successfully");
      } else {
        logWriterService.logStringToConsoleOutput("Message sending failed: " + cause);
        throw new RabbitMQException("Could not sent message");
      }
    });
    return rabbitTemplate;
  }
}
