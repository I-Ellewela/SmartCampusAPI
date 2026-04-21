package com.smartcampus.exception;

/**
 * Thrown when a client attempts to DELETE a room that still has sensors assigned.
 * Mapped to HTTP 409 Conflict.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}
