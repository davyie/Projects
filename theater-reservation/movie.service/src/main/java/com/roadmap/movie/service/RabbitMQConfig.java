package com.roadmap.movie.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Swap the names in the future
     */
    private String exchange = "exchange";
    private String queue = "queue";
    private String routingKey = "routingKey";

    @Bean
    public Queue queue() {
        return new Queue(queue);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding queueBinding() {
        return BindingBuilder.bind(queue()).to(directExchange()).with(routingKey);
    }
}
