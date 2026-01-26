package com.roadmap.schedule.service;

import com.roadmap.schedule.service.exceptions.DuplicateEntryException;
import com.roadmap.schedule.service.exceptions.OverlappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

@RestControllerAdvice
public class GlobalExceptionControllerHandler {

    @ExceptionHandler(OverlappingException.class)
    public ProblemDetail handleOverlappingTimeslot(OverlappingException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_ACCEPTABLE,
                exception.getMessage()
        );

        problemDetail.setTitle("The interval overlapped");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElement(NoSuchElementException noSuchElementException) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                noSuchElementException.getMessage()
        );

        problemDetail.setTitle("Element not found");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleIncorrectArgument(HttpMessageNotReadableException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_ACCEPTABLE,
                exception.getMessage()
        );

        problemDetail.setTitle("Something is missing in request");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingRequestParam(MissingServletRequestParameterException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_ACCEPTABLE,
                exception.getMessage()
        );
        problemDetail.setTitle("Request parameter is missing");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ProblemDetail handleDuplicateEntry(DuplicateEntryException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_ACCEPTABLE,
                exception.getMessage()
        );

        problemDetail.setTitle("Duplicate Entry");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
