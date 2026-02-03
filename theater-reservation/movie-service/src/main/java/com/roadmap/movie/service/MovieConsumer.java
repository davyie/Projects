package com.roadmap.movie.service;

import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.Disposable;
import replys.Reply;
import replys.ReplyType;
import requests.Request;
import tools.jackson.databind.ObjectMapper;

@Service
public class MovieConsumer {

    private MovieRepository movieRepository;

    private Logger LOG = LoggerFactory.getLogger(MovieConsumer.class);

    // Mappers
    private MovieMapper movieMapper;
    private ObjectMapper objectMapper;

    public MovieConsumer(
                         MovieRepository movieRepository,
                         MovieMapper movieMapper,
                         ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.objectMapper = objectMapper;

    }

    @RabbitListener(queues = "queue")
    public void receive(Request request) {
        LOG.info(request.getMessage());
    }

    @Transactional
    private Reply handleGetById(String correlationId, Long id) {
        Reply reply = new Reply();

        reply.setCorrelationId(correlationId);
        reply.setMessage("This is message from MovieConsumer, handleGetById");
        reply.setReplyType(ReplyType.GET_MOVIE_BY_ID);
        MovieDTO dto = null;
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
        reply.setPayload(dto);
        return reply;
    }

    @Transactional
    private Reply handleGetAll(String correlationId) {
        Reply reply = new Reply();

        reply.setCorrelationId(correlationId);
        reply.setMessage("This is the reply from MovieConsumer to fetch all movies.");
        reply.setReplyType(ReplyType.GET_ALL_MOVIES);

//        reply.setPayload(dtos);
        return reply;
    }
}
