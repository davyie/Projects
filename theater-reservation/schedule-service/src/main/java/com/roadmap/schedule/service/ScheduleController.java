package com.roadmap.schedule.service;

import com.roadmap.schedule.service.exceptions.DuplicateEntryException;
import com.roadmap.schedule.service.exceptions.OverlappingException;
import dto.ScheduleDTO;
import dto.TimeSlotDTO;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ScheduleEntity> createSchedule(@PathVariable Long movieId) throws DuplicateEntryException {
        ScheduleEntity schedule = scheduleService.createSchedule(movieId);
        return ResponseEntity.status(HttpStatus.OK).body(schedule);
    }

    @DeleteMapping("/schedule")
    public ResponseEntity<String> deleteSchedule(@RequestParam Long scheduleId) {

        if (scheduleService.deleteScheduleById(scheduleId)) {
            return ResponseEntity.status(HttpStatus.OK).body("Successful!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Unsuccessful!");
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
        return ResponseEntity.status(HttpStatus.OK).body(entity);
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

    @GetMapping("/error")
    public ResponseEntity<String> errorEndpoint() throws RuntimeException {
        throw new RuntimeException("error endpoint");
    }
}
