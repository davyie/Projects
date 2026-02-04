package com.roadmap.schedule.service;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotEntityRepository extends R2dbcRepository<TimeSlotEntity, Long> {
}
