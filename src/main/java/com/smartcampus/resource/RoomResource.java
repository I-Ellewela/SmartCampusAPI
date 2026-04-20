package com.smartcampus.resource;

import com.smartcampus.dao.MockDatabase;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Part 2 — Room Management Resource.
 * Manages /api/v1/rooms
 *
 * Lifecycle: JAX-RS creates a NEW instance of this class per request (request-scoped).
 * All data lives in MockDatabase's static maps, so state is preserved across requests.
 */
@Path("/rooms")
public class RoomResource {

    // -------------------------------------------------------------------------
    // Part 2.1 — GET /api/v1/rooms  →  list all rooms
    // -------------------------------------------------------------------------
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(MockDatabase.ROOMS.values());
        return Response.ok(rooms).build();
    }

    // -------------------------------------------------------------------------
    // Part 2.1 — POST /api/v1/rooms  →  create a new room
    // Returns 201 Created with a Location header pointing to the new resource.
    // -------------------------------------------------------------------------
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\":\"Room ID must be provided.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
        if (MockDatabase.ROOMS.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                           .entity("{\"error\":\"A room with this ID already exists.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }

        // Ensure sensorIds list is initialised
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        MockDatabase.ROOMS.put(room.getId(), room);

        // Also initialise an empty readings bucket placeholder
        URI location = UriBuilder.fromPath("/api/v1/rooms/{id}").build(room.getId());
        return Response.created(location).entity(room).build();
    }

    // -------------------------------------------------------------------------
    // Part 2.1 — GET /api/v1/rooms/{roomId}  →  fetch one room by ID
    // -------------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = MockDatabase.ROOMS.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room with ID '" + roomId + "' was not found.");
        }
        return Response.ok(room).build();
    }

    // -------------------------------------------------------------------------
    // Part 2.1 — PUT /api/v1/rooms/{roomId}  →  update a room's details
    // -------------------------------------------------------------------------
    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existing = MockDatabase.ROOMS.get(roomId);
        if (existing == null) {
            throw new DataNotFoundException("Room with ID '" + roomId + "' was not found.");
        }
        updatedRoom.setId(roomId);
        // Preserve the sensor list so we don't accidentally orphan sensors
        if (updatedRoom.getSensorIds() == null || updatedRoom.getSensorIds().isEmpty()) {
            updatedRoom.setSensorIds(existing.getSensorIds());
        }
        MockDatabase.ROOMS.put(roomId, updatedRoom);
        return Response.ok(updatedRoom).build();
    }

    // -------------------------------------------------------------------------
    // Part 2.2 — DELETE /api/v1/rooms/{roomId}
    // Business rule: cannot delete a room that still has sensors assigned.
    // Throws RoomNotEmptyException → mapped to 409 Conflict.
    //
    // Idempotency: DELETE is idempotent. The first call removes the room (204).
    // Subsequent identical calls find nothing and throw DataNotFoundException
    // which returns 404. The server state is unchanged after the first call.
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = MockDatabase.ROOMS.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room with ID '" + roomId + "' was not found.");
        }

        // Safety check: block deletion if sensors are still assigned
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "'. It still has " +
                room.getSensorIds().size() + " sensor(s) assigned. " +
                "Remove all sensors before decommissioning the room."
            );
        }

        MockDatabase.ROOMS.remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }
}
