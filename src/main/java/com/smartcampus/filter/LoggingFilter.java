package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS logging filter for enterprise observability.
 *
 * Implements BOTH ContainerRequestFilter (inbound) and ContainerResponseFilter (outbound).
 * Registered globally via @Provider so it intercepts every request and response
 * without touching any business logic — the filter doesn't care whether the
 * request succeeded or resulted in an exception.
 *
 * In a production environment these logs would be piped into aggregation tools
 * such as the ELK Stack (Elasticsearch, Logstash, Kibana) or AWS CloudWatch.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info("--- Incoming Request ---");
        LOGGER.info("Method : " + requestContext.getMethod());
        LOGGER.info("URI    : " + requestContext.getUriInfo().getAbsolutePath());
        LOGGER.info("Headers: " + requestContext.getHeaders());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info("--- Outgoing Response ---");
        LOGGER.info("Status : " + responseContext.getStatus());
    }
}
