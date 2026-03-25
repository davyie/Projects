package com.roadmap.movie.core.mapper;

import com.roadmap.movie.core.domain.Movie;
import dto.MovieDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovieMapper {

    MovieDTO toDto(Movie movie);

    List<MovieDTO> toDtoList(List<Movie> movies);
}
