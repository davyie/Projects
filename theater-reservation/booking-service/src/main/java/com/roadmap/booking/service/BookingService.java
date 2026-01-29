package com.roadmap.booking.service;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class BookingService {

    private BookingRepository bookingRepository;
    private ObjectMapper objectMapper;
    public BookingService(BookingRepository bookingRepository,
                          ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.objectMapper = objectMapper;
    }


    public BookingDTO createBooking(BookingDTO dto) {
        BookingEntity entity = objectMapper.convertValue(dto, BookingEntity.class);
        // Send message to movie to see if it exists. Perform logic
        // Send message to schedule to see if the timeslot exist. Perform logic
        return dto;
    }
}
