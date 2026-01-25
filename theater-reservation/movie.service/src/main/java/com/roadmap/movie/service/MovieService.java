package com.roadmap.movie.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This is a class which handles most of the logic for communicating between
 * database and other services using rabbitmq.
 */
@Service
public class MovieService {

    private MovieRepository movieRepository;

    private RabbitTemplate rabbitTemplate;

    public MovieService(MovieRepository movieRepository,
                        RabbitTemplate rabbitTemplate) {
        this.movieRepository = movieRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public void addMovie(Movie movie) {
        movieRepository.save(movie);
    }

    public void sendMessage() {
        rabbitTemplate.convertAndSend(
                "exchange",
                "routingKey",
                "Hello from Movie Service"
        );
    }
}
