package com.roadmap.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import requests.Request;

@Service
public class Producer {

    private RabbitTemplate rabbitTemplate;

    private Logger LOG = LoggerFactory.getLogger(Producer.class);

    public Producer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Request request) {
        LOG.info(request.toString());
        LOG.info("The correlationId for outbound message: " + request.getCorrelationId());
        rabbitTemplate.convertAndSend(
                "exchange",
                "routingKey",
                request
        );
    }
}
