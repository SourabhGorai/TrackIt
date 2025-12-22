package com.trackIt.sla_service.exception;

public class NotFoundException extends ServiceException {
    public NotFoundException(String type, String name) {
        super(String.format("%s not found: '%s'", type, name));
    }
}
