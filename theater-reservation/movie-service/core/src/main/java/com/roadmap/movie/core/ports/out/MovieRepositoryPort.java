package com.roadmap.movie.core.ports.out;

import com.roadmap.movie.core.domain.Movie;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieRepositoryPort {
    Flux<Movie> findAll();
    Mono<Movie> findById(Integer id);
    Mono<Movie> save(Movie movie);
    Mono<Void> deleteById(Integer id);
}
