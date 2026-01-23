package com.roadmap.schedule.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Instant startTime;
    private Duration duration;
    private Instant endTime;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    @JsonBackReference
    private ScheduleEntity schedule;

    @Override
    public String toString() {
        return "ID: " + id + " date: " + date.toString() + " Start Time: " + startTime.toString() + " schedule id: " + schedule.getId();
    }

    public boolean isOverlapping(TimeSlotEntity other) {
        return this.getStartTime().isBefore(other.getEndTime())
                && other.getStartTime().isBefore(this.getStartTime());
    }
}
