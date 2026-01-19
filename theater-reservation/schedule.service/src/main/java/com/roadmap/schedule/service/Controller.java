package com.roadmap.schedule.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import requests.GetAllMoviesRequest;

import java.util.UUID;

@RestController
@RequestMapping("/schedule")
public class Controller {

    private Producer producer;

    public Controller(Producer producer) {
        this.producer = producer;
    }

    @PostMapping("/message")
    public ResponseEntity<String> sendMessage() {
        GetAllMoviesRequest request = new GetAllMoviesRequest();
        request.setCorrelationId(UUID.randomUUID().toString());
        request.setMessage("This message is from Schedule controller");
        producer.sendMessage(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
