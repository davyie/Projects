package com.roadmap.schedule.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("timeslots")
public class TimeSlotEntity {

    @Id
    private Long id;

    private LocalDate date;
    private Instant startTime;
    private Duration duration;
    private Instant endTime;

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
