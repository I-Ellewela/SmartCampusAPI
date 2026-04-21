package com.smartcampus.exception;

/**
 * Thrown when a requested resource (room or sensor by ID) is not found.
 * Mapped to HTTP 404 Not Found.
 */
public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
