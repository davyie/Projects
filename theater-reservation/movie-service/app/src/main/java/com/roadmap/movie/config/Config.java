package com.roadmap.movie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.movie.core.domain.Movie;
import com.roadmap.movie.core.mapper.MovieMapper;
import dto.MovieDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

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
}
