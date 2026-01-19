package com.roadmap.movie.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import replys.GetAllMovieReply;
import requests.GetAllMoviesRequest;

import java.util.List;

@Service
public class MovieConsumer {

    private RabbitTemplate rabbitTemplate;

    private MovieRepository movieRepository;

    public MovieConsumer(RabbitTemplate rabbitTemplate, MovieRepository movieRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.movieRepository = movieRepository;
    }

    @RabbitListener(queues = "queue")
    public void receive(GetAllMoviesRequest request) {
        System.out.println(request.getMessage());

        List<Movie> movies = movieRepository.findAll();

        GetAllMovieReply reply = new GetAllMovieReply();
        reply.setCorrelationId(request.getCorrelationId());
        reply.setMessage("This is the reply from MovieConsumer");

        rabbitTemplate.convertAndSend(
            "replyExchange",
                "replyRoutingKey",
                reply
        );
    }
}
