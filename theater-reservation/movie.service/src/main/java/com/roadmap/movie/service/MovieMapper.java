package com.roadmap.movie.service;

import dto.MovieDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieDTO toDto(Movie movie);

    List<MovieDTO> toDtoList(List<Movie> movies);
}
