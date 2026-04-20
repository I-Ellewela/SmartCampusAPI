package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1.2 — Discovery Endpoint.
 *
 * GET /api/v1
 * Returns a JSON object with API metadata: version, contact info,
 * and a map of primary resource collection URIs (HATEOAS principle).
 *
 * HATEOAS (Hypermedia As The Engine Of Application State) benefits:
 * - Client developers can navigate the API dynamically without consulting
 *   static documentation — the response itself tells them where to go next.
 * - Reduces coupling: if a URI changes, clients following links automatically
 *   adapt rather than breaking because they had the path hardcoded.
 * - Enables API discoverability and self-documentation at runtime.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact",     "admin@smartcampus.ac.uk");

        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("resources", links);

        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("documentation", "https://smartcampus.api/docs");
        meta.put("status",        "https://smartcampus.api/status");
        response.put("links", meta);

        return Response.ok(response).build();
    }
}
