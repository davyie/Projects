package com.roadmap.schedule.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long movieId; // One movie has one schedule

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TimeSlotEntity> timeSlotList;

    public TimeSlotEntity addTimeSlot(TimeSlotEntity timeSlot) {
        List<TimeSlotEntity> list = this.timeSlotList.stream()
                .filter(ts -> ts.getDate() != timeSlot.getDate() && ts.isOverlapping(timeSlot))
                .toList();
        if( list.size() == 0 ) {
            throw new RuntimeException("Conflict! Interval exists on the given date " +timeSlot.getDate());
        }

        this.timeSlotList.add(timeSlot);

        return timeSlot;
    }

    public boolean deleteTimeSlot(Long timeSlotId) {
        Integer sizeBefore = this.timeSlotList.size();
        return this.timeSlotList.removeIf(ts -> ts.getId() == timeSlotId);
    }
}
