package com.example.cassandraui.exception;

import com.datastax.oss.driver.api.core.DriverException;
import com.example.cassandraui.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotConnectedException.class)
    public ResponseEntity<ErrorResponse> handleNotConnected(NotConnectedException ex) {
        return error(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        return error("Invalid request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DriverException.class)
    public ResponseEntity<ErrorResponse> handleDriver(DriverException ex) {
        return error("Cassandra error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return error("Unexpected error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status.value(), Instant.now()));
    }
}
