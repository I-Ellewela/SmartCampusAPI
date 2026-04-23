# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures  
**Student:** E.K Ishini Upekha Ellewela  
**Student ID:** 20230898  
**Year:** 2025/26

---

## API Overview

A RESTful JAX-RS web service built with Jersey 2.32 and Jackson for the University of Westminster's Smart Campus initiative. The API manages campus **Rooms** and IoT **Sensors** (CO2 monitors, occupancy trackers, temperature sensors), maintains historical **Sensor Readings**, and enforces business-rule constraints with structured JSON error responses.

### Base URL
```
http://localhost:8080/SmartCampusAPI/api/v1
```

### Project Structure
```
SmartCampusAPI/
├── pom.xml
└── src/main/
    ├── java/com/smartcampus/
    │   ├── application/   SmartCampusApplication.java
    │   ├── dao/           MockDatabase.java
    │   ├── exception/     5 custom exceptions + 5 ExceptionMappers
    │   ├── filter/        LoggingFilter.java
    │   ├── model/         Room, Sensor, SensorReading, ErrorMessage
    │   └── resource/      DiscoveryResource, RoomResource, SensorResource,
    │                      SensorReadingResource
    └── webapp/WEB-INF/    web.xml
```

---

## Build & Run Instructions

### Prerequisites
- Java 8 or higher
- Apache Maven 3.6+
- Apache Tomcat 9.x

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/I-Ellewela/SmartCampusAPI
cd SmartCampusAPI
```

**2. Build the WAR file**
```bash
mvn clean package
```
The WAR file will be generated at: `target/SmartCampusAPI-1.0-SNAPSHOT.war`

**3. Deploy to Tomcat**

Copy the WAR into Tomcat's webapps directory:
```bash
cp target/SmartCampusAPI-1.0-SNAPSHOT.war /path/to/tomcat/webapps/SmartCampusAPI.war
```

**4. Start Tomcat**
```bash
/path/to/tomcat/bin/startup.sh       # Linux/Mac
/path/to/tomcat/bin/startup.bat      # Windows
```

**5. Verify the server is running**

Open a browser or run:
```bash
curl http://localhost:8080/SmartCampusAPI/api/v1
```

---

## Sample curl Commands

### 1. Discovery — GET /api/v1
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 \
     -H "Accept: application/json"
```

### 2. List All Rooms — GET /api/v1/rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Accept: application/json"
```

### 3. Create a Room — POST /api/v1/rooms
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"id":"CS-201","name":"Seminar Room","capacity":40}'
```

### 4. Register a Sensor — POST /api/v1/sensors
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"CO2-005","type":"CO2","status":"ACTIVE","currentValue":390.0,"roomId":"CS-201"}'
```

### 5. Filter Sensors by Type — GET /api/v1/sensors?type=CO2
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2" \
     -H "Accept: application/json"
```

### 6. Add a Sensor Reading — POST /api/v1/sensors/{sensorId}/readings
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-005/readings \
     -H "Content-Type: application/json" \
     -d '{"value":415.3}'
```

### 7. Get Reading History — GET /api/v1/sensors/{sensorId}/readings
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
     -H "Accept: application/json"
```

### 8. Delete a Room with Sensors (Triggers 409) — DELETE /api/v1/rooms/{roomId}
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 9. Register Sensor with Invalid roomId (Triggers 422)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"id":"TEMP-099","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-999"}'
```

### 10. Post Reading to MAINTENANCE Sensor (Triggers 403)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/OCC-001/readings \
     -H "Content-Type: application/json" \
     -d '{"value":25.0}'
```

---

## API Endpoints Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | `/api/v1` | Discovery — API metadata and resource links |
| GET    | `/api/v1/rooms` | List all rooms |
| POST   | `/api/v1/rooms` | Create a new room |
| GET    | `/api/v1/rooms/{roomId}` | Get room by ID |
| PUT    | `/api/v1/rooms/{roomId}` | Update a room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors exist) |
| GET    | `/api/v1/sensors` | List all sensors (supports `?type=` filter) |
| POST   | `/api/v1/sensors` | Register a new sensor (validates roomId) |
| GET    | `/api/v1/sensors/{sensorId}` | Get sensor by ID |
| PUT    | `/api/v1/sensors/{sensorId}` | Update sensor details/status |
| DELETE | `/api/v1/sensors/{sensorId}` | Remove a sensor |
| GET    | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST   | `/api/v1/sensors/{sensorId}/readings` | Add a new reading |
| GET    | `/api/v1/sensors/{sensorId}/readings/{readingId}` | Get a specific reading |

---

## Conceptual Report — Answers to Coursework Questions

---

### Part 1.1 — JAX-RS Resource Lifecycle

**Question:** Explain the default lifecycle of a JAX-RS Resource class. Is a new instance created per request or is it a singleton? How does this impact in-memory data management?

By default, JAX-RS resource classes are **request-scoped**: a brand-new instance of the resource class (e.g., `RoomResource`) is instantiated for every incoming HTTP request and discarded once the response is sent. This is the standard behaviour defined by the JAX-RS specification.

This has a critical architectural implication for in-memory state: **instance-level fields cannot be used to store shared data**, because each request gets its own fresh object and would see an empty state. To solve this, all application data is stored in the **static fields of `MockDatabase`**, which are initialised once at class-load time and shared across all instances and all threads.

However, because multiple requests can arrive simultaneously (concurrent threads each creating their own `RoomResource` instance but all reading/writing the same static maps), raw `HashMap` or `ArrayList` would cause race conditions and data corruption. This is why all collections in `MockDatabase` are `ConcurrentHashMap` instances, which provide thread-safe read/write operations without requiring explicit `synchronized` blocks.

In a production system this would be replaced with a proper database (e.g., JPA/Hibernate with a connection pool), which handles concurrency at the persistence layer.

---

### Part 1.2 — HATEOAS and the Discovery Endpoint

**Question:** Why is Hypermedia (HATEOAS) considered a hallmark of advanced RESTful design? How does it benefit client developers?

HATEOAS (Hypermedia As The Engine Of Application State) is the principle that API responses should include **links to related actions and resources**, so clients can navigate the API dynamically at runtime rather than relying on hardcoded URLs from static documentation.

Benefits for client developers:

1. **Discoverability**: A client hitting `GET /api/v1` immediately learns the available resource paths (`/api/v1/rooms`, `/api/v1/sensors`) without needing to read any external documentation.
2. **Reduced coupling**: If a URI structure changes (e.g., `/api/v2/rooms`), clients following HATEOAS links adapt automatically. Clients with hardcoded paths break.
3. **Self-documenting APIs**: The response itself describes the API surface, reducing onboarding time for new developers.
4. **Workflow guidance**: Responses can include links to next valid actions, guiding clients through correct usage sequences.

This is why REST's inventor Roy Fielding considered HATEOAS essential to the REST architectural style — without it, an API is not truly RESTful, merely "HTTP-based."

---

### Part 2.1 — IDs vs Full Objects in List Responses

**Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning full room objects?

**Returning only IDs** (e.g., `["LIB-301", "LAB-101"]`):
- Minimises network payload — very fast for large collections
- Forces clients to make N additional GET requests to fetch details (N+1 problem)
- Increases server load and latency for clients needing full data

**Returning full objects** (e.g., the entire Room JSON):
- Higher payload size — acceptable for small-to-medium collections
- Client gets all needed data in a single round-trip
- Preferred when clients consistently need full data (e.g., rendering a room list with name, capacity, sensor count)

This API returns **full objects** from `GET /api/v1/rooms` because facilities managers need all room metadata at once. For very large datasets, pagination and field projection (e.g., `?fields=id,name`) would be the production solution.

---

### Part 2.2 — DELETE Idempotency

**Question:** Is the DELETE operation idempotent? Justify what happens across multiple identical DELETE requests.

**Yes, DELETE is idempotent** as defined by HTTP RFC 7231 — repeated calls produce the same server state.

In this implementation:
- **First DELETE** of `LIB-301` (with no sensors): removes it from `MockDatabase.ROOMS`, returns `204 No Content`.
- **Second identical DELETE**: `MockDatabase.ROOMS.get("LIB-301")` returns `null`, throws `DataNotFoundException`, returns `404 Not Found`.

The server state after the first call is "room does not exist." The second call also results in the room not existing. The **server state is unchanged** — this satisfies idempotency. The HTTP status code differs (204 vs 404), but idempotency concerns server state, not response codes. This is standard and correct REST behaviour.

---

### Part 3.1 — @Consumes and Content-Type Mismatch

**Question:** Explain the technical consequences if a client sends data in a different format (e.g., `text/plain` or `application/xml`) to a method annotated with `@Consumes(APPLICATION_JSON)`.

When a JAX-RS resource method is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the runtime inspects the `Content-Type` header of every incoming request before the method is invoked. If the header value does not match — for example, a client sends `Content-Type: text/plain` — JAX-RS **immediately returns HTTP `415 Unsupported Media Type`** without ever calling the method. The request body is not parsed; the method is never entered; no business logic is executed.

This is a clean, declarative content negotiation mechanism. The annotation acts as a contract: "this endpoint only accepts JSON." The `415` response tells the client exactly what went wrong and what to fix, enabling fast debugging without server-side logging or exception handling.

---

### Part 3.2 — @QueryParam vs @PathParam for Filtering

**Question:** Contrast query parameter filtering (`?type=CO2`) with path-based filtering (`/sensors/type/CO2`). Why is the query parameter approach generally superior?

| Aspect | `@QueryParam` (`?type=CO2`) | Path segment (`/sensors/type/CO2`) |
|--------|----------------------------|--------------------------------------|
| Semantics | Describes how to filter a collection | Implies `type/CO2` is a distinct resource |
| Composability | `?type=CO2&status=ACTIVE` easily combines filters | `/sensors/type/CO2/status/ACTIVE` is unwieldy |
| Optionality | `@QueryParam` is naturally optional — omitting it returns all items | Path params are required (part of the URI) |
| REST conventions | RFC 3986: query strings are for refinement, not resource identification | Path segments should identify resources, not filter criteria |
| Caching | Same base URI (`/sensors`) — easier to cache | Different URIs per filter combination — harder to cache |
| Tooling | Swagger/OpenAPI models query params natively | Path-based filters require bespoke documentation |

Query parameters are semantically correct for **filtering, sorting, and searching** existing collections. Path segments should identify **resources** in a hierarchy. Using `@QueryParam` is the industry-standard RESTful approach.

---

### Part 4.1 — Sub-Resource Locator Pattern

**Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern compared to defining all nested paths in one class.

The Sub-Resource Locator pattern allows a resource method to **return an object** (rather than a response), delegating all further request handling to that object. In this API, `SensorResource` contains:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

JAX-RS then inspects `SensorReadingResource` to match the remaining path and HTTP method.

**Benefits:**

1. **Single Responsibility**: `SensorResource` handles sensor CRUD only. `SensorReadingResource` handles all reading history logic. Each class has one job.
2. **Reduced complexity**: A single "god class" handling all nested paths (`/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, `/sensors/{id}/readings/{rid}`) would quickly become unmaintainable. Delegation keeps each class small and focused.
3. **Testability**: `SensorReadingResource` can be unit-tested independently by constructing it directly with a `sensorId` — no need to involve the HTTP layer or `SensorResource`.
4. **Reusability**: The sub-resource class could theoretically be reused in other contexts if the business model required it.
5. **Scalability**: As the API grows, new sub-resources (e.g., `SensorAlertResource`) can be added without modifying existing classes.

---

### Part 5.2 — Why 422 is More Accurate than 404

**Question:** Why is HTTP 422 Unprocessable Entity more semantically accurate than 404 when the issue is a missing reference inside a valid JSON payload?

When a client POSTs a new sensor with a `roomId` of `"FAKE-999"`:

- **The endpoint exists** — `POST /api/v1/sensors` resolves correctly. A `404` would suggest the endpoint itself was not found, which is misleading and incorrect.
- **The JSON is syntactically valid** — `400 Bad Request` (malformed syntax) would also be inaccurate.
- **The problem is semantic**: the request body is well-formed, but its content references a resource (`roomId: "FAKE-999"`) that does not exist in the system. The server cannot process the entity as submitted.

`422 Unprocessable Entity` was designed precisely for this scenario — the request is syntactically valid but semantically invalid. It communicates clearly: "I understood what you sent, but I cannot fulfil it because the data is logically inconsistent." This helps client developers distinguish between:
- Wrong endpoint (404)
- Malformed JSON (400)
- Logically invalid content (422)

---

### Part 5.4 — Security Risks of Exposing Stack Traces

**Question:** From a cybersecurity standpoint, explain the risks of exposing Java stack traces to external API consumers.

A raw Java stack trace such as:
```
java.lang.NullPointerException
  at com.smartcampus.dao.MockDatabase.getRoomById(MockDatabase.java:47)
  at com.smartcampus.resource.RoomResource.deleteRoom(RoomResource.java:82)
```

exposes multiple categories of exploitable information:

1. **Internal class names and package structure** (`com.smartcampus.dao.MockDatabase`): Attackers learn the exact architecture and can craft targeted exploits or search for known vulnerabilities in those specific classes.
2. **File names and line numbers** (`MockDatabase.java:47`): Reveals the precise location of logic, helping attackers reverse-engineer business rules.
3. **Framework and library versions**: Stack frames from Jersey, Jackson, or Tomcat classes expose exact dependency versions, enabling attackers to look up published CVEs (Common Vulnerabilities and Exposures) for those exact versions.
4. **Logic flow disclosure**: The call stack reveals the exact sequence of method calls, exposing internal processing logic that attackers can use to find injection points or bypass validation.
5. **Database schema hints**: ORM-generated traces (Hibernate, JPA) often include table names, column names, and query structures.

The `GlobalExceptionMapper` in this API solves this by logging the full stack trace **server-side only** (via `java.util.logging`) while returning a sanitised, generic `500` message to the client — protecting internals without losing debuggability for system administrators.

---

### Part 5.5 — JAX-RS Logging Filters

**Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

Using a JAX-RS filter for logging is advantageous for several important reasons:

1. **Single Responsibility Principle**: Resource classes should handle business logic only. Embedding logging statements inside every resource method violates the Single Responsibility Principle — the class now has two jobs: handle the request AND log it. A dedicated filter separates these concerns cleanly.

2. **No Code Duplication**: With manual logging, every single resource method (GET rooms, POST rooms, GET sensors, POST sensors, etc.) needs its own `Logger.info()` calls. If the log format ever changes, every method must be updated individually. A single filter handles all requests automatically with zero duplication.

3. **Guaranteed Coverage**: A manual approach risks developers forgetting to add logging to new methods as the API grows. A filter registered with `@Provider` intercepts every single request and response automatically — it is impossible to miss an endpoint.

4. **Consistency**: All log entries follow exactly the same format because they all pass through the same filter code, making logs easier to parse and analyse with aggregation tools like ELK Stack (Elasticsearch, Logstash, Kibana) or AWS CloudWatch.

5. **Separation of Concerns**: Logging is a cross-cutting concern — it applies uniformly across the entire application, not to any specific business operation. JAX-RS filters are designed precisely for cross-cutting concerns, alongside authentication, CORS headers, and response compression.

The `LoggingFilter` in this API implements both `ContainerRequestFilter` and `ContainerResponseFilter`, logging the HTTP method, full URI, and response status code for every single interaction without touching any resource class.

---

## Error Response Format

All errors return a consistent JSON structure:

```json
{
  "errorMessage": "Human-readable description of the problem",
  "errorCode": 409,
  "documentation": "https://smartcampus.api/docs/errors#room-not-empty"
}
```

| Scenario | HTTP Status | Exception |
|----------|-------------|-----------|
| Room/Sensor not found by ID | 404 Not Found | `DataNotFoundException` |
| Delete room with sensors assigned | 409 Conflict | `RoomNotEmptyException` |
| Sensor registered with non-existent roomId | 422 Unprocessable Entity | `LinkedResourceNotFoundException` |
| Post reading to MAINTENANCE sensor | 403 Forbidden | `SensorUnavailableException` |
| Any unexpected runtime error | 500 Internal Server Error | `GlobalExceptionMapper<Throwable>` |

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 8+ | Core language |
| JAX-RS (Jersey) | 2.32 | REST framework |
| Jackson | Via Jersey | JSON serialisation |
| Apache Tomcat | 9.x | Servlet container |
| Maven | 3.6+ | Build tool |
