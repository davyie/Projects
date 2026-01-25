package com.roadmap.schedule.service;

import dto.ScheduleDTO;
import dto.TimeSlotDTO;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    private ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.status(HttpStatus.OK).body("Hello from Schedule Service");
    }

    @GetMapping("/all")
    public ResponseEntity<List<ScheduleEntity>> getAllSchedules() {
        return ResponseEntity.status(HttpStatus.OK).body(scheduleService.getSchedules());
    }

    @GetMapping("/by/movie/{movieId}")
    public ResponseEntity<String> getByMovieId() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Not implemented yet");
    }

    @PostMapping("/schedule/{movieId}")
    public ResponseEntity<ScheduleEntity> createSchedule(@PathVariable Long movieId) {
        try {
            ScheduleEntity schedule = scheduleService.createSchedule(movieId);

            if (schedule == null) {
                // This covers the case where the schedule already exists
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            return ResponseEntity.ok(schedule);

        } catch (RuntimeException e) {
            // This catches the "Movie does not exist!" error from our logic
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (TimeoutException e) {
            // This catches the case where RabbitMQ/Movie Service didn't reply in time
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (Exception e) {
            // General error handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<String> deleteSchedule(@RequestParam Long scheduleId) {

        if (scheduleService.deleteScheduleById(scheduleId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Succesful!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Unsuccesful!");
        }
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
    public ResponseEntity<TimeSlotEntity> addTimeSlot(@RequestParam Long scheduleId, @RequestBody TimeSlotDTO timeSlotDTO) {

        TimeSlotEntity entity = scheduleService.addTimeSlot(scheduleId, timeSlotDTO);
        System.out.println(entity);
        if (entity == null) {return ResponseEntity.status(HttpStatus.CONFLICT).body(null);}
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/timeslot")
    public ResponseEntity<String> deleteTimeSlot(@RequestParam Long scheduleId, @RequestParam Long timeSlotId) {
        System.out.println(scheduleId);
        System.out.println(timeSlotId);
        if (scheduleService.deleteTimeSlotFromSchedule(scheduleId, timeSlotId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Succesful!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Timeslot not found");
    }
}
