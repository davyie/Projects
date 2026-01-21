package com.roadmap.schedule.service;

import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import replys.Reply;
import replys.ReplyType;

import java.util.HashMap;
import java.util.List;

@Service
public class Consumer {

    private Logger LOG = LoggerFactory.getLogger(Consumer.class);

    @RabbitListener(queues = "replyQueue")
    public void receive(Reply reply) {
        LOG.info(reply.getMessage());
        LOG.info("The correlation id for inbound/message message: " + reply.getCorrelationId());

        LOG.info(String.valueOf(reply.getPayload() == null));
        if (reply.getPayload() == null) { return; } // If there is nothing in the payload then we skip it.

        if (reply.getPayload().containsKey("movie")) {
            LOG.info(String.valueOf(MovieDTO.class.cast(reply.getPayload().get("movie"))));
        } else {
            List<MovieDTO> list = List.class.cast(reply.getPayload().get("movies"));
            for (MovieDTO dto : list) {
                LOG.info(dto.getName());
            }
        }
    }
}
