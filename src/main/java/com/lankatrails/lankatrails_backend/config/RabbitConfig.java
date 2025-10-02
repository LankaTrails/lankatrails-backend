package com.lankatrails.lankatrails_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
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

    @Bean
    public Queue chatQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Queue typingQueue() {
        return new AnonymousQueue();
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(chatQueue).to(chatExchange).with("chat.room.*");
    }

    @Bean
    public Binding typingBinding(Queue typingQueue, TopicExchange chatExchange) {
        return BindingBuilder.bind(typingQueue).to(chatExchange).with("chat.typing.*");
    }

}

