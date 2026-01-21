package com.roadmap.movie.service;

import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import replys.Reply;
import replys.ReplyType;
import requests.Request;
import requests.RequestType;

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

    public MovieConsumer(RabbitTemplate rabbitTemplate,
                         MovieRepository movieRepository,
                         MovieMapper movieMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    @RabbitListener(queues = "queue")
    public void receive(Request request) {

        if (!request.getServices().contains(util.Service.MOVIE_SERVICE)) {return;}

        Reply reply;

        switch (request.getRequestType()) {
            case GET_MOVIE_BY_ID -> {
                reply = handleGetById(request.getCorrelationId(), Long.valueOf((Integer) request.getPayload().get("movieId")));
                LOG.info("Get movie by id message handles here");
            }
            case GET_ALL_MOVIES -> reply = handleGetAll(request.getCorrelationId());
            default -> {
                return;
            }
        }

        rabbitTemplate.convertAndSend(
            "replyExchange",
                "replyRoutingKey",
                reply
        );
    }

    private Reply handleGetById(String correlationId, Long id) {
        Reply reply = new Reply();
        Map<String, Object> payload = new HashMap<>();

        reply.setCorrelationId(correlationId);
        reply.setMessage("This is message from MovieConsumer, handleGetById");
        reply.setReplyType(ReplyType.GET_MOVIE_BY_ID);
        payload.put("movie", movieMapper.toDto(movieRepository.findById(id).orElseThrow()));
        return reply;
    }

    private Reply handleGetAll(String correlationId) {
        Reply reply = new Reply();
        Map<String, Object> payload = new HashMap<>();

        reply.setCorrelationId(correlationId);
        reply.setMessage("This is the reply from MovieConsumer to fetch all movies.");
        reply.setReplyType(ReplyType.GET_ALL_MOVIES);
        List<MovieDTO> movies = movieMapper.toDtoList(movieRepository.findAll());
        payload.put("movies", movies);
        reply.setPayload(payload);
        return reply;
    }
}
