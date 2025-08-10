package com.lankatrails.lankatrails_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "chat.exchange";

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // Each backend instance can have its own queue. For dev use AnonymousQueue.
    @Bean
    public Queue instanceQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding instanceBinding(Queue instanceQueue, TopicExchange chatExchange) {
        // subscribe to all room keys (safer)
        return BindingBuilder.bind(instanceQueue).to(chatExchange).with("chat.room.#");
    }
}

