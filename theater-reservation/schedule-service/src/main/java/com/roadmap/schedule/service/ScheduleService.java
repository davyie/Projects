package com.roadmap.schedule.service;

import com.roadmap.schedule.service.exceptions.DuplicateEntryException;
import dto.ScheduleDTO;
import dto.TimeSlotDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
    public Mono<ScheduleDTO> createSchedule(Long movieId) {
        return scheduleRepository.existsByMovieId(movieId)
                .flatMap( exists -> {
                    if (exists) {
                        return Mono.error(
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Schedule for movie " + movieId + " already exists"
                                )
                        );
                    }
                    return sendMessage(movieId)
                            .doOnNext( reply -> {LOG.info("Success in Movie Service {}", reply.getMessage());})
                            .flatMap( reply -> {
                                ScheduleEntity schedule = new ScheduleEntity();
                                schedule.setMovieId(movieId);
                                return scheduleRepository.save(schedule);
                            });
                })
                .map( s -> objectMapper.convertValue(s, ScheduleDTO.class));
    }

    private Mono<Reply> sendMessage(Long movieId) {
        Request request = new Request();
        request.setServices(new HashSet<>(Arrays.asList(util.Service.MOVIE_SERVICE)));
        request.setRequestType(RequestType.GET_MOVIE_BY_ID);
        request.setMessage("This is a message from Schedule Service");
        request.setCorrelationId(UUID.randomUUID().toString());
        request.setPayload(movieId);
        return producer.sendMessage(request);
    }

    @Transactional
    public Mono<TimeSlotDTO> addTimeSlot(Long scheduleId, TimeSlotDTO timeSlotDTO) {
        return null;
    }

    public Mono<Boolean> deleteTimeSlotFromSchedule(Long scheduleId, Long timeSlotId) {
        Mono<ScheduleEntity> schedule = null;
        try {
            schedule = scheduleRepository.findById(scheduleId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mono.justOrEmpty(false);
    }

    public Mono<ScheduleEntity> getScheduleByMovieId(Long movieId) {
        // Find schedule by movie id;
        return scheduleRepository.findByMovieId(movieId);
    }

    public Flux<ScheduleDTO> getSchedules() {
        return scheduleRepository.findAll().map(s -> objectMapper.convertValue(s, ScheduleDTO.class));
    }

    public Mono<Boolean> deleteScheduleById(Long scheduleId) {
        return scheduleRepository.deleteById(scheduleId)
                .thenReturn(Boolean.TRUE)
                .doOnError(e -> LOG.error("Unable to find schedule with id: {} \n Stack: {}", scheduleId, e.getMessage()));
    }


    public Mono<ScheduleEntity> updateSchedule(Long scheduleId, ScheduleDTO dto) {
        Mono<ScheduleEntity> entity = null;
        try {entity = scheduleRepository.findById(scheduleId);}
        catch (Exception e) {e.printStackTrace();}

        // Convert dto to schedule object
        ScheduleEntity mappedSchedule = objectMapper.convertValue(dto, ScheduleEntity.class);
        return null;
    }
}
