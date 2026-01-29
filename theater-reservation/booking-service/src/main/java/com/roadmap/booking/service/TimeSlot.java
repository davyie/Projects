package com.roadmap.booking.service;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private LocalDate date;
    private Instant startTime;
    private Instant endTime;
    private Duration duration;

    @OneToMany
    @JoinColumn(name = "booking_id")
    @JsonBackReference
    private List<BookingEntity> bookings;
}
