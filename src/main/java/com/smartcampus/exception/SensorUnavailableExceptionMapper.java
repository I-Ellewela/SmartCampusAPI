package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps SensorUnavailableException → HTTP 403 Forbidden.
 * Returned when a POST reading targets a sensor in MAINTENANCE status.
 */
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorMessage error = new ErrorMessage(
            exception.getMessage(),
            403,
            "https://smartcampus.api/docs/errors#sensor-unavailable"
        );
        return Response.status(Status.FORBIDDEN)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
