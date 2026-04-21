package com.smartcampus.exception;

/**
 * Thrown when a client POSTs a Sensor with a roomId that does not exist.
 * Mapped to HTTP 422 Unprocessable Entity — the request body is syntactically
 * valid JSON but semantically invalid (references a non-existent resource).
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
