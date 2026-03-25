package com.roadmap.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Service
public class BookingService {

    private BookingRepository bookingRepository;
    private ObjectMapper objectMapper;
    private Logger LOG = LoggerFactory.getLogger(BookingService.class);

    public BookingService(BookingRepository bookingRepository,
                          ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.objectMapper = objectMapper;
    }


    public Mono<BookingDTO> createBooking(BookingDTO dto) {
        BookingEntity entity = objectMapper.convertValue(dto, BookingEntity.class);
        // Send message to movie to see if it exists. Perform logic
        // Send message to schedule to see if the timeslot exist. Perform logic
        bookingRepository.save(entity).subscribe();
        return Mono.justOrEmpty(dto);
    }

    public Flux<BookingDTO> getAllBookings() {
        return bookingRepository.findAll()
                .map(b -> objectMapper.convertValue(b, BookingDTO.class));
    }
}
