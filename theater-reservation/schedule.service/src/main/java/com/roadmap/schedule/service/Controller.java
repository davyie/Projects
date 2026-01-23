package com.roadmap.schedule.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import requests.Request;
import requests.RequestType;
import util.Service;

import java.util.*;

public class Controller {

    private Producer producer;

    public Controller(Producer producer) {
        this.producer = producer;
    }

    @PostMapping("/message/{choice}")
    public ResponseEntity<String> sendMessage(@PathVariable String choice) {

        Request request = new Request();;
        if (choice.equals("1")) {
            request.setRequestType(RequestType.GET_ALL_MOVIES);
        } else if (choice.equals("2")) {
            request.setRequestType(RequestType.GET_MOVIE_BY_ID);
            request.setPayload(1);
        }

        request.setServices(new HashSet<>(Arrays.asList(Service.MOVIE_SERVICE)));
        request.setCorrelationId(UUID.randomUUID().toString());
        request.setMessage("This message is from Schedule controller");

        producer.sendMessage(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
