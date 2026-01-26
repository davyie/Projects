package com.roadmap.schedule.service.exceptions;

public class OverlappingException extends RuntimeException {
    public OverlappingException(String message) {
        super(message);
    }
}
