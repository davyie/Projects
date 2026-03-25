package com.roadmap.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import replys.Reply;
import tools.jackson.databind.ObjectMapper;

@Service
public class Consumer {

    private Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private ObjectMapper objectMapper;

    public Consumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "replyQueue")
    public void receive(Reply reply) {
        LOG.info(reply.getMessage());
        LOG.info(reply.getCorrelationId());
    }
}
