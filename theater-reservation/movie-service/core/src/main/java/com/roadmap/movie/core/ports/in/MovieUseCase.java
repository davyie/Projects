package com.roadmap.movie.core.ports.in;

import dto.MovieDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import requests.Request;

public interface MovieUseCase {

    Flux<MovieDTO> getAllMovies();

    Mono<MovieDTO> getMovieById(Integer movieId);

    Mono<MovieDTO> addMovie(MovieDTO dto);

    Mono<MovieDTO> updateMovie(Integer movieId, MovieDTO dto);

    Mono<Void> deleteMovie(Integer movieId);

    Mono<Request> sendMessage();
}
