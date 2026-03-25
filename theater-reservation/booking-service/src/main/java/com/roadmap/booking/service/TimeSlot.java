package com.roadmap.booking.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Table("timeslots")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    private Long id;
    private LocalDate date;
    private Instant startTime;
    private Instant endTime;
    private Duration duration;

    private List<BookingEntity> bookings;
}
