package com.roadmap.booking.service;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/helloworld")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello from Booking Controller");
    }

    @GetMapping("/hellomono")
    public Mono<String> helloWorldAsync() {
        return Mono.justOrEmpty("Hello with Mono").delayElement(Duration.ofMillis(500));
    }

    // Create booking
    // Delete booking

    @PostMapping("/")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO dto) {
        bookingService.createBooking(dto);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
}
