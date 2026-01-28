package com.roadmap.schedule.service;

import com.roadmap.schedule.service.exceptions.DuplicateEntryException;
import dto.ScheduleDTO;
import dto.TimeSlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import replys.Reply;
import requests.Request;
import requests.RequestType;
import tools.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.*;

@Service
public class ScheduleService {

    private ScheduleRepository scheduleRepository;
    private ObjectMapper objectMapper;
    private Producer producer;

    private Logger LOG = LoggerFactory.getLogger(ScheduleService.class);

    private final ConcurrentMap<String, CompletableFuture<Reply>> concurrentMap = new ConcurrentHashMap<>();

    public ScheduleService(ScheduleRepository scheduleRepository,
                           ObjectMapper objectMapper,
                           Producer producer) {
        this.scheduleRepository = scheduleRepository;
        this.objectMapper = objectMapper;
        this.producer = producer;
    }

    @Transactional
    public ScheduleEntity createSchedule(Long movieId) {
        // 2, check if movie id exists. Send message to movie service
        Reply reply = sendMessage(movieId);
        LOG.info(reply.getPayload().toString());

        // 1, check if a schedule for movie exists
        LOG.info("Check if we have a schedule with the movieId");
        if (scheduleRepository.existsByMovieId(movieId)) {throw new DuplicateEntryException("Schedule for the movie exists");}

        // 3, create the schedule
        ScheduleEntity schedule = new ScheduleEntity();
        schedule.setMovieId(movieId);
        schedule.setTimeSlotList(new ArrayList<>());
        scheduleRepository.save(schedule);
        return schedule;
    }

    private Reply sendMessage(Long movieId) {
        Request request = new Request();
        request.setServices(new HashSet<>(Arrays.asList(util.Service.MOVIE_SERVICE)));
        request.setRequestType(RequestType.GET_MOVIE_BY_ID);
        request.setCorrelationId(UUID.randomUUID().toString());
        request.setPayload(movieId);

//        CompletableFuture<Reply> futureReply = new CompletableFuture<>();
//        concurrentMap.put(request.getCorrelationId(), futureReply);

        return producer.sendMessage(request);
    }

    @Transactional
    public TimeSlotEntity addTimeSlot(Long scheduleId, TimeSlotDTO timeSlotDTO) {

        TimeSlotEntity timeslot = objectMapper.convertValue(timeSlotDTO, TimeSlotEntity.class);

        ScheduleEntity schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new NoSuchElementException("No object is found with schedule id: " + scheduleId));

        List<TimeSlotEntity> timeslots = schedule.getTimeSlotList();
        if (timeslots == null) {return timeslot;}

        timeslot.setSchedule(schedule);
        schedule.addTimeSlot(timeslot); // Throws exception

        scheduleRepository.save(schedule);
        LOG.info("Added Timeslot to List<> and added it to schedule object. Saved it to database.");
        return timeslot;
    }

    public boolean deleteTimeSlotFromSchedule(Long scheduleId, Long timeSlotId) {
        ScheduleEntity schedule = null;
        try {
            schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (schedule == null) {return false;}
        schedule.deleteTimeSlot(timeSlotId);
        scheduleRepository.save(schedule);
        return true;
    }

    public ScheduleEntity getScheduleByMovieId(Long movieId) {
        // Find schedule by movie id;
        return scheduleRepository.findByMovieId(movieId).orElseThrow(() -> new NoSuchElementException("No schedule related to movie id " + movieId));
    }

    public List<ScheduleEntity> getSchedules() {
        return scheduleRepository.findAll();
    }

    public boolean deleteScheduleById(Long scheduleId) {
        try {
            scheduleRepository.deleteById(scheduleId);
            return true;
        } catch (RuntimeException re) {
            return false;
        }
    }


    public ScheduleEntity updateSchedule(Long scheduleId, ScheduleDTO dto) {
        ScheduleEntity entity = null;
        try {entity = scheduleRepository.findById(scheduleId).orElseThrow();}
        catch (Exception e) {e.printStackTrace();}

        // Convert dto to schedule object
        ScheduleEntity mappedSchedule = objectMapper.convertValue(dto, ScheduleEntity.class);
        return null;
    }
}
