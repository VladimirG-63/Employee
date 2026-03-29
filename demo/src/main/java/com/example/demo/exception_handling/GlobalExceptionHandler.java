package com.example.demo.exception_handling;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Map<String, String> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    public Map<String, String> handleValidation(RuntimeException ex) {
        return buildErrorResponse(ex.getMessage());
    }

    private Map<String, String> buildErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}