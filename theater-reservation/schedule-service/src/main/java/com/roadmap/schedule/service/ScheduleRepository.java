package com.roadmap.schedule.service;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.Future;

@Repository
public interface ScheduleRepository extends R2dbcRepository<ScheduleEntity, Long> {
    Mono<ScheduleEntity> findByMovieId(Long movieId);
    Mono<Boolean> existsByMovieId(Long movieId);
}
