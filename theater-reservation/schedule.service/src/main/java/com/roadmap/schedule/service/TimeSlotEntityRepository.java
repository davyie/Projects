package com.roadmap.schedule.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotEntityRepository extends JpaRepository<TimeSlotEntity, Long> {
}
