package com.roadmap.movie.service;

import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import replys.Reply;
import replys.ReplyType;
import requests.Request;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieConsumer {

    private RabbitTemplate rabbitTemplate;

    private MovieRepository movieRepository;

    private Logger LOG = LoggerFactory.getLogger(MovieConsumer.class);

    // Mappers
    private MovieMapper movieMapper;
    private ObjectMapper objectMapper;

    public MovieConsumer(RabbitTemplate rabbitTemplate,
                         MovieRepository movieRepository,
                         MovieMapper movieMapper,
                         ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "queue")
    public Reply receive(Request request) {

        if (!request.getServices().contains(util.Service.MOVIE_SERVICE)) {return null;}

        Reply reply;

        switch (request.getRequestType()) {
            case GET_MOVIE_BY_ID -> {
                Long id = objectMapper.convertValue(request.getPayload(), Long.class);
                reply = handleGetById(request.getCorrelationId(), id);
                LOG.info("Get movie by id message handles here");
            }
            case GET_ALL_MOVIES -> {
                reply = handleGetAll(request.getCorrelationId());
                LOG.info("Handle the case where we get all movies");
            }
            default -> {
                return null;
            }
        }

        return reply;
//        rabbitTemplate.convertAndSend(
//            "replyExchange",
//                "replyRoutingKey",
//                reply
//        );
    }

    @Transactional
    private Reply handleGetById(String correlationId, Long id) {
        Reply reply = new Reply();

        reply.setCorrelationId(correlationId);
        reply.setMessage("This is message from MovieConsumer, handleGetById");
        reply.setReplyType(ReplyType.GET_MOVIE_BY_ID);
        MovieDTO dto = null;
        try {
            dto = movieMapper.toDto(movieRepository.findById(id).orElseThrow());
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

        List<MovieDTO> dtos = movieMapper.toDtoList(movieRepository.findAll());
        reply.setPayload(dtos);
        return reply;
    }
}
