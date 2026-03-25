package com.roadmap.booking.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long userId;
    private TimeSlot timeSlot;
    private Long movieId;
    private String description;
}
