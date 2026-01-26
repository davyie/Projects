package com.roadmap.schedule.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.Future;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    Optional<ScheduleEntity> findByMovieId(Long movieId);
    boolean existsByMovieId(Long movieId);
}
