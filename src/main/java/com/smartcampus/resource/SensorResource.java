package com.smartcampus.resource;

import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 3 & 4 — Sensor Resource.
 * Manages /api/v1/sensors
 *
 * Also contains the Sub-Resource Locator (Part 4.1) that delegates
 * /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
 */
@Path("/sensors")
public class SensorResource {

    // -------------------------------------------------------------------------
    // Part 3.2 — GET /api/v1/sensors  →  list sensors, optional ?type= filter
    //
    // @QueryParam approach is superior to path-based filtering (e.g. /sensors/type/CO2)
    // because query parameters are designed for optional, non-hierarchical filtering of
    // collections. They do not imply a new resource level in the URI hierarchy, are
    // composable (e.g. ?type=CO2&status=ACTIVE), and are semantically correct per
    // RFC 3986. Path segments should identify resources, not describe filter criteria.
    // -------------------------------------------------------------------------
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(MockDatabase.SENSORS.values());

        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensors) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }

        return Response.ok(sensors).build();
    }

    // -------------------------------------------------------------------------
    // Part 3.1 — POST /api/v1/sensors  →  register a new sensor
    //
    // Validates that the roomId in the request body actually exists.
    // If not → LinkedResourceNotFoundException → 422 Unprocessable Entity.
    //
    // @Consumes(APPLICATION_JSON): If a client sends a different Content-Type
    // (e.g. text/plain or application/xml), JAX-RS immediately returns
    // 415 Unsupported Media Type without invoking the method at all.
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSensor(Sensor sensor) {
        // Part 3.1 — Foreign key integrity check
        if (sensor.getRoomId() == null || sensor.getRoomId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"roomId is required.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }

        Room room = MockDatabase.ROOMS.get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: room with ID '" + sensor.getRoomId() +
                "' does not exist in the system. " +
                "Please create the room first or supply a valid roomId."
            );
        }

        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Sensor ID must be provided.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }

        if (MockDatabase.SENSORS.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\":\"A sensor with this ID already exists.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }

        // Default status to ACTIVE if not supplied
        if (sensor.getStatus() == null || sensor.getStatus().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        MockDatabase.SENSORS.put(sensor.getId(), sensor);

        // Link sensor to its room
        room.getSensorIds().add(sensor.getId());

        // Initialise an empty readings list for this sensor
        MockDatabase.READINGS.put(sensor.getId(), new ArrayList<>());

        URI location = UriBuilder.fromPath("/api/v1/sensors/{id}").build(sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}  →  fetch one sensor by ID
    // -------------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = MockDatabase.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        return Response.ok(sensor).build();
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/sensors/{sensorId}  →  update sensor details / status
    // -------------------------------------------------------------------------
    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {
        Sensor existing = MockDatabase.SENSORS.get(sensorId);
        if (existing == null) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }
        updatedSensor.setId(sensorId);
        MockDatabase.SENSORS.put(sensorId, updatedSensor);
        return Response.ok(updatedSensor).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/sensors/{sensorId}  →  remove sensor and unlink from room
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = MockDatabase.SENSORS.get(sensorId);
        if (sensor == null) {
            throw new DataNotFoundException("Sensor with ID '" + sensorId + "' was not found.");
        }

        // Unlink from its room
        Room room = MockDatabase.ROOMS.get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        MockDatabase.SENSORS.remove(sensorId);
        MockDatabase.READINGS.remove(sensorId);

        return Response.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Part 4.1 — Sub-Resource Locator
    //
    // GET/POST /api/v1/sensors/{sensorId}/readings
    //
    // This method does NOT handle the HTTP request itself. Instead it returns
    // an instance of SensorReadingResource, and JAX-RS delegates further
    // method matching (GET, POST, etc.) to that class.
    //
    // Architectural benefit: Separating reading logic into its own class keeps
    // SensorResource focused on sensor-level CRUD. SensorReadingResource handles
    // all the historical data complexity. This reduces coupling, improves
    // testability, and makes large APIs manageable — each class has one responsibility.
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
