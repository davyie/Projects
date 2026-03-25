package com.roadmap.schedule.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.roadmap.schedule.service.exceptions.OverlappingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("schedules")
public class ScheduleEntity {

    @Id
    private Long id;
    private Long movieId; // One movie has one schedule
}
