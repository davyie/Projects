package com.roadmap.movie.core.service;

import com.roadmap.movie.core.domain.Movie;
import com.roadmap.movie.core.ports.in.MovieUseCase;
import com.roadmap.movie.core.ports.out.MessagePublisherPort;
import com.roadmap.movie.core.ports.out.MovieRepositoryPort;
import dto.MovieDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import requests.Request;
import requests.RequestType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class MovieDomainService implements MovieUseCase {

    private final Logger LOG = LoggerFactory.getLogger(MovieDomainService.class);

    private final MovieRepositoryPort movieRepositoryPort;

    private final MessagePublisherPort messagePublisherPort;

    private final ObjectMapper objectMapper;

    public MovieDomainService(MovieRepositoryPort movieRepositoryPort,
            MessagePublisherPort messagePublisherPort,
            ObjectMapper objectMapper) {
        this.movieRepositoryPort = movieRepositoryPort;
        this.messagePublisherPort = messagePublisherPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Override
    public Flux<MovieDTO> getAllMovies() {
        return movieRepositoryPort.findAll()
                .doOnNext(m -> LOG.info("Fetched {} to send as response", m.toString()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    @Override
    public Mono<MovieDTO> addMovie(MovieDTO dto) {
        Movie movie = objectMapper.convertValue(dto, Movie.class);
        return movieRepositoryPort.save(movie)
                .doOnSuccess((movieSaved) -> LOG.info("Success. {}", movieSaved.toString()))
                .doOnError(e -> LOG.info(e.getMessage()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    @Override
    public Mono<MovieDTO> updateMovie(Integer movieId, MovieDTO dto) {
        return movieRepositoryPort.findById(movieId)
                .flatMap(m -> {
                    m.setName(dto.getName());
                    m.setScreenTime(dto.getScreenTime());
                    m.setDescription(dto.getDescription());
                    return movieRepositoryPort.save(m);
                })
                .doOnSuccess(updateMovie -> {
                    if (updateMovie != null) {
                        LOG.info("Movie update: {}", updateMovie.toString());
                    } else {
                        LOG.info("Movie was not found with id: {}", movieId);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    LOG.error("Movie with id: {} doesn't exists", movieId);
                    return Mono.empty();
                }))
                .doOnError(e -> LOG.error(e.getMessage()))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }

    @Transactional
    @Override
    public Mono<Void> deleteMovie(Integer movieId) {
        return movieRepositoryPort
                .deleteById(movieId)
                .doOnSuccess(aVoid -> {
                    LOG.info("Deleted product with id: {}", movieId);
                })
                .doOnError(e -> {
                    LOG.error(e.getMessage());
                });
    }

    @Override
    public Mono<Request> sendMessage() {
        Request request = new Request(
                UUID.randomUUID().toString(),
                "This is a message from Movie Service",
                RequestType.GET_MOVIE_BY_ID,
                null,
                null
        );
        return messagePublisherPort.publishMessage(request)
                .thenReturn(request);
    }

    @Override
    public Mono<MovieDTO> getMovieById(Integer movieId) {
        return movieRepositoryPort.findById(movieId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(m -> objectMapper.convertValue(m, MovieDTO.class));
    }
}
