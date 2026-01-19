package com.roadmap.schedule.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import requests.GetAllMoviesRequest;

@Service
public class Producer {

    private RabbitTemplate rabbitTemplate;

    public Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(GetAllMoviesRequest request) {
        rabbitTemplate.convertAndSend(
                "exchange",
                "routingKey",
                request
        );
    }
}
