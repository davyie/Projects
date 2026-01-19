package com.roadmap.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    private Logger LOG = LoggerFactory.getLogger(Consumer.class);

    @RabbitListener(queues = "queue")
    public void receive(Message message) {
        LOG.info(message.toString());
    }
}
