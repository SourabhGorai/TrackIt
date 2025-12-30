package com.trackIt.incidents.exception;

import com.trackIt.incidents.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleAlreadyExists(AlreadyExistsException ex) {

        log.warn(ex.getMessage()); // ✅ clean log, no stack trace

        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException ex) {

        log.warn(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404
                .body(ApiResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleServiceException(ServiceException ex) {

        log.error(ex.getMessage()); // internal error, still no stack trace

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500
                .body(ApiResponse.failure("Something went wrong. Please try again."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {

        log.error("Unexpected error occurred");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Unexpected server error"));
    }
}
