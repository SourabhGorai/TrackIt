package com.trackIt.independent_services.exception;

public class NotFoundException extends ServiceException {
    public NotFoundException(String type, String name) {
        super(String.format("%s not found: '%s'", type, name));
    }
}
