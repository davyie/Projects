package com.roadmap.movie.service;

import dto.MovieDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import requests.Request;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class Config {

    @Bean
    public MovieMapper movieMapper() {
        return new MovieMapper() {
            @Override
            public MovieDTO toDto(Movie movie) {
                return new MovieDTO(movie.getName(), movie.getScreenTime(), movie.getDescription());
            }

            @Override
            public List<MovieDTO> toDtoList(List<Movie> movies) {
                return movies.stream().map(this::toDto).toList();
            }
        };
    }

    @Bean
    public Consumer<Flux<Request>> processRequest(MovieRepository movieRepository) {
        return requestFlux -> requestFlux
                .doOnNext(request -> movieRepository.findById((Long) request.getPayload()))
                .subscribe();
    }
}
