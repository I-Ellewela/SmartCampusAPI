package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps RoomNotEmptyException → HTTP 409 Conflict.
 * Returned when a room with active sensors is targeted for deletion.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorMessage error = new ErrorMessage(
            exception.getMessage(),
            409,
            "https://smartcampus.api/docs/errors#room-not-empty"
        );
        return Response.status(Status.CONFLICT)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
