package com.smartcampus.dao;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory data store for the Smart Campus API.
 *
 * All collections are static so they are shared across all request-scoped
 * resource instances. ConcurrentHashMap is used to prevent race conditions
 * when multiple requests read/write simultaneously.
 *
 * In a production system these would be replaced by a proper database (e.g. JPA/Hibernate).
 */
public class MockDatabase {

    // Rooms keyed by room ID
    public static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

    // Sensors keyed by sensor ID
    public static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();

    // Readings keyed by sensorId → ordered list of readings
    public static final Map<String, List<SensorReading>> READINGS = new ConcurrentHashMap<>();

    static {
        // --- Seed Rooms ---
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        Room r3 = new Room("HALL-A", "Main Lecture Hall A", 200);
        ROOMS.put(r1.getId(), r1);
        ROOMS.put(r2.getId(), r2);
        ROOMS.put(r3.getId(), r3);

        // --- Seed Sensors ---
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 21.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 412.0, "LAB-101");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "HALL-A");
        SENSORS.put(s1.getId(), s1);
        SENSORS.put(s2.getId(), s2);
        SENSORS.put(s3.getId(), s3);

        // Link sensors to their rooms
        ROOMS.get("LIB-301").getSensorIds().add("TEMP-001");
        ROOMS.get("LAB-101").getSensorIds().add("CO2-001");
        ROOMS.get("HALL-A").getSensorIds().add("OCC-001");

        // --- Seed Readings ---
        List<SensorReading> r1Readings = new ArrayList<>();
        r1Readings.add(new SensorReading(20.1));
        r1Readings.add(new SensorReading(21.5));
        READINGS.put("TEMP-001", r1Readings);

        List<SensorReading> r2Readings = new ArrayList<>();
        r2Readings.add(new SensorReading(400.0));
        r2Readings.add(new SensorReading(412.0));
        READINGS.put("CO2-001", r2Readings);

        READINGS.put("OCC-001", new ArrayList<>());
    }
}
