package com.trackIt.independent_services.exception;

public class AlreadyExistsException extends ServiceException {
    public AlreadyExistsException(String type, String data) {
        super(String.format("%s already exists: '%s'", type, data));
    }
}
