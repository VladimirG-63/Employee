package com.example.demo.exception_handling;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}