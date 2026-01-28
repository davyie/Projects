package com.roadmap.schedule.service.exceptions;

import org.springframework.dao.DataAccessException;

public class DuplicateEntryException extends DataAccessException {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
