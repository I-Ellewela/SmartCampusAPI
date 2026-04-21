package com.smartcampus.exception;

/**
 * Thrown when a POST reading is attempted on a sensor with status "MAINTENANCE".
 * Mapped to HTTP 403 Forbidden — the server understands the request but refuses
 * to fulfil it due to the sensor's current operational state.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}
