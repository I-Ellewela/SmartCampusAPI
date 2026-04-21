package com.smartcampus.exception;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * Global catch-all ExceptionMapper<Throwable>.
 *
 * Intercepts ANY unexpected runtime error (NullPointerException,
 * IndexOutOfBoundsException, etc.) and returns a clean HTTP 500 response.
 *
 * CRITICAL SECURITY RULE: The real stack trace is ONLY logged internally
 * (server-side). It is NEVER sent to the client. Exposing stack traces
 * would reveal internal class names, library versions, file paths, and
 * business logic flow — information that attackers exploit for targeted attacks.
 *
 * JAX-RS uses a "closest match" algorithm, so specific mappers (404, 409, 422, 403)
 * take priority. This mapper only fires for anything not already mapped.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full stack trace internally for debugging by sysadmins
        LOGGER.severe("Unexpected server error: " + exception.getMessage());
        exception.printStackTrace(); // Goes to server console/log file only

        // Return a sanitised, generic message to the client
        ErrorMessage error = new ErrorMessage(
            "An unexpected internal server error occurred. Please contact support.",
            500,
            "https://smartcampus.api/docs/errors#internal-server-error"
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(error)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
