package com.roadmap.movie.providers.database;

import com.roadmap.movie.core.domain.Movie;
import com.roadmap.movie.core.ports.out.MovieRepositoryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class MovieRepositoryAdapter implements MovieRepositoryPort {

    private final MovieRepository repository;

    public MovieRepositoryAdapter(MovieRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Movie> findAll() {
        return repository.findAll();
    }

    @Override
    public Mono<Movie> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    public Mono<Movie> save(Movie movie) {
        return repository.save(movie);
    }

    @Override
    public Mono<Void> deleteById(Integer id) {
        return repository.deleteById(id);
    }
}
