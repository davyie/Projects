package com.roadmap.schedule.service;

import com.roadmap.schedule.service.exceptions.DuplicateEntryException;
import com.roadmap.schedule.service.exceptions.OverlappingException;
import dto.ScheduleDTO;
import dto.TimeSlotDTO;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/hello")
    public Mono<String> helloWorld() {
        return Mono.justOrEmpty("Hello World!");
    }

    @GetMapping("/all")
    public Flux<ScheduleDTO> getAllSchedules() {
        return scheduleService.getSchedules();
    }

    @GetMapping("/by/movie/{movieId}")
    public ResponseEntity<String> getByMovieId() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Not implemented yet");
    }

    @PostMapping("/schedule/{movieId}")
    public Mono<ScheduleDTO> createSchedule(@PathVariable Long movieId) throws DuplicateEntryException {
        return scheduleService.createSchedule(movieId).doOnError(e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("/schedule")
    public Mono<Boolean> deleteSchedule(@RequestParam Long scheduleId) {
        return scheduleService.deleteScheduleById(scheduleId);
    }

    @PutMapping("/schedule/{scheduleId}")
    public ResponseEntity<String> updateSchedule(@PathVariable Long scheduleId, @RequestBody ScheduleDTO dto) {
        scheduleService.updateSchedule(scheduleId, dto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Test endpoint to see how to send timeslot objects
     */
    @PostMapping("/timeslot")
    public Mono<TimeSlotDTO> addTimeSlot(@RequestParam Long scheduleId, @RequestBody TimeSlotDTO timeSlotDTO) {
        return Mono.justOrEmpty(timeSlotDTO);
    }

    @DeleteMapping("/timeslot")
    public Mono<Boolean> deleteTimeSlot(@RequestParam Long scheduleId, @RequestParam Long timeSlotId) {
        return Mono.justOrEmpty(Boolean.FALSE);
    }

    @GetMapping("/error")
    public ResponseEntity<String> errorEndpoint() throws RuntimeException {
        throw new RuntimeException("error endpoint");
    }
}
