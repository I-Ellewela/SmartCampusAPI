package com.smartcampus.resource;

import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 4.2 — Historical Data Management sub-resource.
 *
 * This class is NOT annotated with @Path at class level.
 * It is returned by SensorResource's sub-resource locator method,
 * and the JAX-RS runtime injects the sensorId context at that point.
 *
 * Handles all operations under: /api/v1/sensors/{sensorId}/readings
 */
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings  →  fetch full reading history
    // -------------------------------------------------------------------------
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        // Confirm parent sensor exists
        if (!MockDatabase.SENSORS.containsKey(sensorId)) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        List<SensorReading> readings = MockDatabase.READINGS.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(readings).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings  →  append a new reading
    //
    // Business rules:
    //  1. Parent sensor must exist.
    //  2. Sensor must NOT be in MAINTENANCE status → 403 Forbidden.
    //  3. On success: reading is stored AND parent sensor's currentValue is updated.
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = MockDatabase.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        // Part 5.3 — State constraint: MAINTENANCE sensors cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and " +
                "cannot accept new readings. Please reconnect the device first."
            );
        }

        // Assign a UUID and timestamp if not provided by client
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading
        MockDatabase.READINGS.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // Part 4.2 Side Effect — update parent sensor's currentValue for data consistency
        sensor.setCurrentValue(reading.getValue());
        MockDatabase.SENSORS.put(sensorId, sensor);

        URI location = UriBuilder.fromPath("/api/v1/sensors/{sid}/readings/{rid}")
                                 .build(sensorId, reading.getId());
        return Response.created(location).entity(reading).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings/{readingId}  →  single reading
    // -------------------------------------------------------------------------
    @GET
    @Path("/{readingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadingById(@PathParam("readingId") String readingId) {
        if (!MockDatabase.SENSORS.containsKey(sensorId)) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        List<SensorReading> readings = MockDatabase.READINGS.getOrDefault(sensorId, new ArrayList<>());
        for (SensorReading r : readings) {
            if (r.getId().equals(readingId)) {
                return Response.ok(r).build();
            }
        }
        throw new DataNotFoundException("Reading with ID '" + readingId + "' not found for sensor '" + sensorId + "'.");
    }
}
