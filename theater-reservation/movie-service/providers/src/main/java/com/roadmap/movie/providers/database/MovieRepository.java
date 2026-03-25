package com.roadmap.movie.providers.database;

import com.roadmap.movie.core.domain.Movie;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends R2dbcRepository<Movie, Integer> {
}
