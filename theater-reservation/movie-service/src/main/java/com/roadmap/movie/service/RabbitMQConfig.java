package com.roadmap.movie.service;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RabbitMQConfig {

    /**
     * Swap the names in the future. Store them in application.properties
     */
    private String exchange = "exchange";
    private String queue = "queue";
    private String routingKey = "routingKey";

    private String replyQueue = "replyQueue";
    private String replyExchange = "replyExchange";
    private String replyRoutingKey = "replyRoutingKey";


    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

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

    @Bean
    public Queue replyQueue() {
        return new Queue(replyQueue);
    }

    @Bean
    public DirectExchange replyExchange() {
        return new DirectExchange(replyExchange);
    }

    @Bean
    public Binding replyBinding() {
        return BindingBuilder.bind(replyQueue()).to(replyExchange()).with(replyRoutingKey);
    }
}
