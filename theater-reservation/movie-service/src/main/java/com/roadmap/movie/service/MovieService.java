package com.roadmap.movie.service;

import com.rabbitmq.client.AMQP;
import dto.MovieDTO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import replys.Reply;
import replys.ReplyType;
import requests.Request;
import requests.RequestType;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;


/**
 * This is a class which handles most of the logic for communicating between
 * database and other services using rabbitmq.
 */
@Service
public class MovieService {

    private Logger LOG = LoggerFactory.getLogger(MovieService.class);

    private MovieRepository movieRepository;

    private RabbitTemplate rabbitTemplate;

    private ObjectMapper objectMapper;


    public MovieService(MovieRepository movieRepository,
                        RabbitTemplate rabbitTemplate,
                        ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Flux<MovieDTO> getAllMovies() {
        return movieRepository.findAll()
                .doOnNext(m -> LOG.info("Fetched {} to send as response", m.toString()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    public Mono<MovieDTO> addMovie(MovieDTO dto) {
        Movie movie = objectMapper.convertValue(dto, Movie.class);
        return movieRepository.save(movie)
                .doOnSuccess((movieSaved) -> LOG.info("Success. {}", movieSaved.toString()))
                .doOnError(e -> LOG.info(e.getMessage()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    public Mono<MovieDTO> updateMovie(Long movieId, MovieDTO dto) {
        return movieRepository.findById(movieId)
                .flatMap(m -> {
                            m.setName(dto.getName());
                            m.setScreenTime(dto.getScreenTime());
                            m.setDescription(dto.getDescription());
                            return movieRepository.save(m);
                        })
                .doOnSuccess(updateMovie -> {
                    if (updateMovie != null) {LOG.info("Movie update: {}", updateMovie.toString());}
                    else {LOG.info("Movie was not found with id: {}", movieId);}
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOG.error("Movie with id: {} doesn't exists", movieId);
                    return Mono.empty();
                }))
                .doOnError(e -> LOG.error(e.getMessage()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    public Mono<Void> deleteMovie(Long movieId) {
        return movieRepository
                .deleteById(movieId)
                .doOnSuccess(aVoid -> {
                    LOG.info("Deleted product with id: {}", movieId);
                })
                .doOnError(e -> {
                    LOG.error(e.getMessage());
                });
    }

    public Mono<Request> sendMessage() {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                    "exchange",
                    "routingKey",
                    new Request(
                            UUID.randomUUID().toString(),
                            "This is a message from Movie Service",
                            RequestType.GET_MOVIE_BY_ID,
                            null,
                            null
                    )
            );
        })
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(new Request(
                        UUID.randomUUID().toString(),
                        "This is a message from Movie Service",
                        RequestType.GET_MOVIE_BY_ID,
                        null,
                        null
                ));
    }

    @RabbitListener(queues = "replyQueue")
    public void receive(Reply reply) {
        LOG.info(reply.getMessage());
    }
}
