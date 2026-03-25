package com.roadmap.booking.service;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEntity {

    @Id
    private Long id;

    private Long userId;

    private TimeSlot timeSlot;

    private Long movieId;
    private String description;
}
