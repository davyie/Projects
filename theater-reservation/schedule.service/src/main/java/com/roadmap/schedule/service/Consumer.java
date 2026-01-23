package com.roadmap.schedule.service;

import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import replys.Reply;
import replys.ReplyType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class Consumer {

    private Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private ObjectMapper objectMapper;

    public Consumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

//    @RabbitListener(queues = "replyQueue")
//    public void receive(Reply reply) {
//        LOG.info(reply.getMessage());
//        LOG.info("The correlation id for inbound/message message: " + reply.getCorrelationId());
//
//        if (reply.getPayload() == null) {  // If there is nothing in the payload then we skip it.
//            LOG.info("The payload is null so we don't need to process it");
//            return;
//        }
//
//        if (reply.getReplyType() == ReplyType.GET_MOVIE_BY_ID) {
//            LOG.info("Get movie by ID");
//            LOG.info(String.valueOf(objectMapper.convertValue(reply.getPayload(), MovieDTO.class)));
//        } else { // findAll
//            LOG.info("Get all movies");
//            LOG.info(String.valueOf(objectMapper.convertValue(
//                    reply.getPayload(),
//                    new TypeReference<List<MovieDTO>>() {})
//            ));
//            List<MovieDTO> dtos = objectMapper.convertValue(reply.getPayload(), new TypeReference<List<MovieDTO>>() {
//            });
//            for (MovieDTO dto : dtos) {
//                LOG.info(dto.toString());
//            }
//        }
//    }
}
