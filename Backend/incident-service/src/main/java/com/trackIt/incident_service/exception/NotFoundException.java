package com.trackIt.incident_service.exception;

public class NotFoundException extends ServiceException {
    public NotFoundException(String type, String name) {
        super(String.format("%s not found with ID: '%s'", type, name));
    }
}
