package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 *
 * 422 is semantically more accurate than 404 here because the request URI
 * itself is valid and found — the problem is inside the JSON body, where the
 * roomId field references a resource that does not exist. A 404 would suggest
 * the endpoint itself was not found, which is misleading.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorMessage error = new ErrorMessage(
            exception.getMessage(),
            422,
            "https://smartcampus.api/docs/errors#linked-resource-not-found"
        );
        return Response.status(422)   // 422 Unprocessable Entity
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
