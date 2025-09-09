package com.pollution.project.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        System.err.println("Unhandled exception: " + ex);

        Map<String, Object> response = new HashMap<>();

        response.put("error", "An unexpected error occurred.");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<?> handleRestClientException(RestClientException ex) {
        System.err.println("RestClientException: " + ex);

        Map<String, Object> response = new HashMap<>();
        response.put("error", "External API error.");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }   
}