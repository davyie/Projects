package com.roadmap.booking.service;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends R2dbcRepository<BookingEntity, Long> {
}
