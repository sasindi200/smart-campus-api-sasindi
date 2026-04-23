# Smart Campus Sensor & Room Management API

## Overview

This project is a RESTful API developed for the **5COSC022W Client-Server Architectures coursework** at the University of Westminster. It models a university's smart campus infrastructure where rooms and sensors are managed through a versioned JAX-RS web service.

The system allows clients to:

- Create and retrieve rooms with capacity metadata
- Register sensors and link them to existing rooms
- Filter sensors by type using query parameters
- Record and retrieve historical sensor readings per sensor
- Enforce business rules such as preventing deletion of occupied rooms
- Receive structured JSON error responses for all error scenarios
- Observe API activity through request and response logging

The API is built entirely using **JAX-RS (Jakarta REST)** with the **Jersey** implementation, packaged as a **WAR** file, and deployed on **Apache Tomcat 9**. In accordance with the coursework requirements, all data is stored using **in-memory data structures only** (`ConcurrentHashMap` and `ArrayList`) — no database is used.

---

## API Design Summary

The API follows a resource-oriented RESTful design centred on three entities:

- **Room** — represents a physical campus space
- **Sensor** — represents a monitoring device installed inside a room
- **SensorReading** — represents a single recorded measurement from a sensor

### Resource Hierarchy

```
GET  /api/v1                              → Discovery endpoint
GET  /api/v1/rooms                        → List all rooms
POST /api/v1/rooms                        → Create a room
GET  /api/v1/rooms/{id}                   → Get a specific room
DELETE /api/v1/rooms/{id}                 → Delete a room (if no sensors assigned)
GET  /api/v1/sensors                      → List all sensors (optional ?type= filter)
POST /api/v1/sensors                      → Register a sensor (roomId must exist)
GET  /api/v1/sensors/{sensorId}/readings  → Get reading history for a sensor
POST /api/v1/sensors/{sensorId}/readings  → Append a new reading (updates currentValue)
```

The hierarchy reflects the physical campus structure. Rooms are top-level resources, sensors belong to rooms, and sensor readings are sub-resources nested under their parent sensor.

---

## Technology Stack

| Component         | Technology                              |
|-------------------|-----------------------------------------|
| Language          | Java 17                                 |
| REST Framework    | JAX-RS (Jakarta REST) via Jersey 3.1.3  |
| DI Container      | Jersey HK2                              |
| JSON Binding      | Jackson (via `jersey-media-json-jackson`) |
| Servlet Container | Apache Tomcat 9                         |
| Build Tool        | Maven                                   |
| Packaging         | WAR                                     |
| Data Storage      | In-memory (`ConcurrentHashMap`)         |

---

## Project Structure

```text
SmartCampusAPI/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/smartcampus/
        │       ├── config/
        │       │   └── AppConfig.java                          ← @ApplicationPath("/api/v1"), package scan
        │       ├── dto/
        │       │   └── ErrorResponse.java                      ← Structured error payload (status + message)
        │       ├── exception/
        │       │   ├── LinkedResourceNotFoundException.java    ← Thrown when sensor's roomId does not exist
        │       │   ├── RoomNotEmptyException.java              ← Thrown when deleting a room with sensors
        │       │   └── SensorUnavailableException.java         ← Thrown when POSTing to a MAINTENANCE sensor
        │       ├── filter/
        │       │   └── LoggingFilter.java                      ← Logs method+URI on request, status on response
        │       ├── mapper/
        │       │   ├── GlobalExceptionMapper.java              ← Catch-all → HTTP 500
        │       │   ├── LinkedResourceNotFoundExceptionMapper.java ← → HTTP 422
        │       │   ├── RoomNotEmptyMapper.java                 ← → HTTP 409
        │       │   └── SensorUnavailableExceptionMapper.java   ← → HTTP 403
        │       ├── model/
        │       │   ├── Room.java                               ← id, name, capacity, sensorIds
        │       │   ├── Sensor.java                             ← id, type, status, currentValue, roomId
        │       │   └── SensorReading.java                      ← id, timestamp (epoch ms), value
        │       ├── resource/
        │       │   ├── DiscoveryResource.java                  ← GET /api/v1
        │       │   ├── RoomResource.java                       ← /api/v1/rooms
        │       │   ├── SensorResource.java                     ← /api/v1/sensors
        │       │   └── SensorReadingResource.java              ← /api/v1/sensors/{id}/readings (sub-resource)
        │       └── store/
        │           └── DataStore.java                          ← Singleton ConcurrentHashMap-based in-memory store
        └── webapp/
            ├── index.html
            ├── META-INF/
            │   └── context.xml
            └── WEB-INF/
                └── web.xml
```

### Package Responsibilities

| Package      | Responsibility |
|--------------|----------------|
| `config`     | Bootstraps the JAX-RS application and sets the versioned base path via `@ApplicationPath` |
| `dto`        | Reusable response wrapper (`ErrorResponse`) for all structured error payloads |
| `exception`  | Domain-specific runtime exceptions thrown by resource classes |
| `filter`     | `ContainerRequestFilter` + `ContainerResponseFilter` for cross-cutting request/response logging |
| `mapper`     | `ExceptionMapper` implementations that convert exceptions into HTTP responses with JSON bodies |
| `model`      | POJO domain entities (`Room`, `Sensor`, `SensorReading`) with full getters and setters |
| `resource`   | JAX-RS resource classes that define REST endpoints and sub-resource locators |
| `store`      | Singleton `DataStore` holding three `ConcurrentHashMap` collections for rooms, sensors, and readings |

---

## Core Data Models

### Room

Represents a physical room on campus.

| Field       | Type           | Description                                          |
|-------------|----------------|------------------------------------------------------|
| `id`        | `String`       | Unique identifier, e.g. `"LIB-301"`                 |
| `name`      | `String`       | Human-readable label, e.g. `"Library Quiet Study"`  |
| `capacity`  | `int`          | Maximum occupancy for safety regulations             |
| `sensorIds` | `List<String>` | IDs of sensors currently deployed in this room       |

### Sensor

Represents a monitoring device installed in a room.

| Field          | Type     | Description                                              |
|----------------|----------|----------------------------------------------------------|
| `id`           | `String` | Unique identifier, e.g. `"TEMP-001"`                    |
| `type`         | `String` | Category, e.g. `"Temperature"`, `"CO2"`, `"Occupancy"` |
| `status`       | `String` | Current state: `"ACTIVE"`, `"MAINTENANCE"`, `"OFFLINE"` |
| `currentValue` | `double` | Most recently recorded measurement                       |
| `roomId`       | `String` | Foreign key referencing the room this sensor belongs to  |

### SensorReading

Represents a historical data point captured by a sensor.

| Field       | Type     | Description                                    |
|-------------|----------|------------------------------------------------|
| `id`        | `String` | Unique reading event ID (UUID recommended)     |
| `timestamp` | `long`   | Epoch time in milliseconds when reading was captured |
| `value`     | `double` | Actual metric value recorded by the hardware   |

---

## How to Build and Run

### Prerequisites

- Java 17 or higher
- Apache Maven 3.6+
- Apache Tomcat 9

### 1. Clone the Repository

```bash
git clone https://github.com/sanularajapaksha-prog/smart-campus-api.git
cd smart-campus-api
```

### 2. Build the Project

```bash
mvn clean package
```

This compiles the project and generates:

```
target/SmartCampusAPI.war
```

### 3. Deploy to Apache Tomcat

Copy the WAR file into your Tomcat `webapps` directory:

**Windows:**
```
copy target\SmartCampusAPI.war C:\apache-tomcat\webapps\
```

**macOS / Linux:**
```bash
cp target/SmartCampusAPI.war /opt/tomcat/webapps/
```

### 4. Start Tomcat

**Windows:**
```bash
C:\apache-tomcat\bin\startup.bat
```

**macOS / Linux:**
```bash
/opt/tomcat/bin/startup.sh
```

### 5. Access the API

Once Tomcat is running, the base URL is:

```
http://localhost:8080/SmartCampusAPI/api/v1
```

---

## API Endpoints Reference

### Discovery

| Method | Path       | Description                                                    |
|--------|------------|----------------------------------------------------------------|
| GET    | `/api/v1`  | Returns API version, admin contact, and links to all resources |

### Rooms — `/api/v1/rooms`

| Method | Path              | Description                                                              |
|--------|-------------------|--------------------------------------------------------------------------|
| GET    | `/`               | Returns all rooms as a JSON array                                        |
| POST   | `/`               | Creates a new room. Requires `id` and `name` fields. Returns `201`       |
| GET    | `/{id}`           | Returns a single room by ID. Returns `404` if not found                  |
| DELETE | `/{id}`           | Deletes a room. Returns `409` if the room still has sensors assigned     |

### Sensors — `/api/v1/sensors`

| Method | Path              | Description                                                              |
|--------|-------------------|--------------------------------------------------------------------------|
| GET    | `/`               | Returns all sensors. Supports optional `?type=` query filter             |
| GET    | `/?type=CO2`      | Returns only sensors matching the given type (case-insensitive)          |
| POST   | `/`               | Registers a new sensor. Returns `422` if the referenced `roomId` is invalid |

### Sensor Readings — `/api/v1/sensors/{sensorId}/readings`

| Method | Path | Description                                                                                        |
|--------|------|----------------------------------------------------------------------------------------------------|
| GET    | `/`  | Returns all historical readings for the sensor. Returns `404` for unknown sensor                   |
| POST   | `/`  | Appends a reading and updates `currentValue` on the parent sensor. Returns `403` if sensor is in `MAINTENANCE` |

---

## Error Handling

The API uses custom exceptions and exception mappers to ensure that all error responses are structured JSON objects. Raw stack traces are never exposed.

### Error Response Format

```json
{
  "status": 409,
  "message": "Cannot delete room with active sensors."
}
```

### Exception Scenarios

| HTTP Status | Exception Class                     | Trigger Scenario                                                |
|-------------|-------------------------------------|-----------------------------------------------------------------|
| `400`       | Inline validation                   | Missing required fields (`id` or `name`) in POST body         |
| `403`       | `SensorUnavailableException`        | POST reading to a sensor with status `MAINTENANCE`             |
| `404`       | Inline `Response.status(404)`       | Room or sensor not found by ID                                 |
| `409`       | `RoomNotEmptyException`             | DELETE a room that still has sensors assigned to it            |
| `422`       | `LinkedResourceNotFoundException`   | POST sensor with a `roomId` that does not exist in the system  |
| `500`       | `GlobalExceptionMapper` (Throwable) | Any unexpected runtime error (NullPointerException, etc.)      |

---

## Logging

The `LoggingFilter` class implements both `ContainerRequestFilter` and `ContainerResponseFilter`, providing observability without modifying any resource method:

- **On every incoming request:** logs the HTTP method and full request URI
- **On every outgoing response:** logs the final HTTP status code

Example log output:
```
INFO: REQ: POST http://localhost:8080/SmartCampusAPI/api/v1/rooms
INFO: RES STATUS: 201
INFO: REQ: GET http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2
INFO: RES STATUS: 200
```

---

## Sample cURL Commands

### 1. Discover the API

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a Room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":40,\"sensorIds\":[]}"
```

### 3. Get All Rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 4. Get a Specific Room

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 5. Register a Sensor (linked to existing room)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":400.0,\"roomId\":\"LIB-301\"}"
```

### 6. Filter Sensors by Type

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 7. Post a Sensor Reading

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"SR-001\",\"timestamp\":1714000000000,\"value\":450.5}"
```

### 8. Get Reading History for a Sensor

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings
```

### 9. Attempt to Delete a Room with Sensors (should return 409)

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 10. Attempt to Register a Sensor with an Invalid Room (should return 422)

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"TEMP-999\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.0,\"roomId\":\"DOES-NOT-EXIST\"}"
```

---

# Conceptual Report

## Part 1 — Service Architecture and Setup

### 1.1 Project and Application Configuration

The application is bootstrapped as a Maven-based JAX-RS service using Jersey 3.1.3 and deployed as a WAR file on Apache Tomcat 10+. The versioned API entry point is defined in `AppConfig.java`, which extends Jersey's `ResourceConfig` and uses the standard `@ApplicationPath("/api/v1")` annotation. The `packages("com.smartcampus")` call instructs Jersey to auto-scan and register all providers, resources, mappers, and filters in the project.

By default, JAX-RS creates a **new resource class instance for each incoming HTTP request**. This per-request lifecycle prevents shared mutable state from being stored inside resource classes, which reduces the risk of data bleeding between concurrent requests.

However, this project stores all application state in a shared `DataStore` singleton. Because multiple requests may read and write the same collections simultaneously, `ConcurrentHashMap` is used for all three stores (`rooms`, `sensors`, `readings`). This provides basic thread safety for individual map operations without external synchronization. In a production system, more granular locking or a proper persistence layer would be required to safely handle compound operations such as "check if room exists, then add sensor and update room's sensorIds list" as an atomic transaction.

### 1.2 Discovery Endpoint

The `GET /api/v1` endpoint is implemented in `DiscoveryResource.java`. It returns a JSON object containing the API version (`1.0.0`), an admin contact address, and a `links` map providing the canonical paths to the `rooms` and `sensors` collections.

Hypermedia (HATEOAS — Hypermedia as the Engine of Application State) is considered an advanced RESTful practice because it allows the API to be self-describing. Instead of requiring clients to read external documentation to know which URLs to call, the server embeds navigable links directly inside responses. This means client developers can discover available actions and transitions at runtime, reducing tight coupling between client and server. As the API evolves, new links can be added to existing responses without breaking existing clients, because clients only follow links they recognise rather than constructing URLs from hard-coded templates.

---

## Part 2 — Room Management

### 2.1 Room Resource Implementation

`RoomResource.java` handles the `/api/v1/rooms` path and supports listing all rooms, creating a new room, retrieving a specific room by ID, and deleting a room.

When returning a list of rooms, the choice between **returning only IDs** versus **returning full room objects** involves a trade-off between bandwidth and client-side work. Returning only IDs minimises response size, which is useful for large collections or bandwidth-constrained clients. However, it forces the client to make additional requests for each room it needs to display. Returning full room objects costs more bandwidth per request but is more convenient and reduces the total number of round trips. In this project, full room objects are returned because the data model is small, the number of rooms is limited, and simplicity and clarity take priority over micro-optimising payload size.

### 2.2 Room Deletion and Safety Logic

The `DELETE /api/v1/rooms/{id}` endpoint enforces a business rule: a room cannot be deleted if it still has sensors registered to it. If `room.getSensorIds()` is non-empty, a `RoomNotEmptyException` is thrown, which the `RoomNotEmptyMapper` converts to a `409 Conflict` response.

The DELETE operation is **idempotent** in this implementation. Idempotency means that making the same request multiple times produces the same system state as making it once. After the first successful deletion, the room no longer exists in the store. Any subsequent DELETE request for the same room ID will receive a `404 Not Found` response. The underlying resource state does not change further — the room remains absent — so the idempotency property holds. The response code may differ between the first call (`204 No Content`) and subsequent calls (`404 Not Found`), but this is acceptable under the HTTP specification, which defines idempotency in terms of server state, not response codes.

---

## Part 3 — Sensor Operations and Linking

### 3.1 Sensor Resource and Integrity

`SensorResource.java` manages the `/api/v1/sensors` collection. When a new sensor is registered via `POST /api/v1/sensors`, the implementation checks whether the specified `roomId` exists in the `DataStore` before proceeding. If the room is not found, a `LinkedResourceNotFoundException` is thrown, which the `LinkedResourceNotFoundExceptionMapper` converts to a `422 Unprocessable Entity` response.

The `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method declares that this endpoint only accepts requests with a `Content-Type: application/json` header. If a client sends a request with a different content type such as `text/plain` or `application/xml`, JAX-RS cannot find a matching method that can consume that media type. The runtime will automatically return a **415 Unsupported Media Type** response without executing any application code. This enforces the API contract and prevents the server from attempting to deserialise incompatible formats.

### 3.2 Filtered Retrieval and Search

The `GET /api/v1/sensors` endpoint accepts an optional `?type=` query parameter annotated with `@QueryParam("type")`. If the parameter is present, the result is filtered using a stream to return only sensors whose `type` field matches the provided value (case-insensitive). If the parameter is absent, the full sensor list is returned.

Using a query parameter for filtering is preferable to embedding the filter in the URL path (e.g. `/api/v1/sensors/type/CO2`) for several reasons. The collection resource — `/api/v1/sensors` — represents all sensors regardless of type. The type filter is a search or narrowing constraint on that collection, not a structurally distinct sub-resource. Query parameters are the established HTTP convention for optional filtering, searching, sorting, and pagination. They can be combined naturally (e.g. `?type=CO2&status=ACTIVE`) without requiring changes to the URL path structure, making the API more extensible as additional filters are added in the future.

---

## Part 4 — Deep Nesting with Sub-Resources

### 4.1 Sub-Resource Locator Pattern

In `SensorResource.java`, the path `/{id}/readings` is handled by a **sub-resource locator** method rather than a direct endpoint method. The locator method is not annotated with an HTTP verb annotation (no `@GET`, `@POST`). Instead, it is annotated with `@Path("/{id}/readings")` and returns a new instance of `SensorReadingResource`, passing the `sensorId` as a constructor argument. JAX-RS then delegates all further routing for that path to the returned object.

This pattern improves maintainability by separating concerns. `SensorResource` stays focused on sensor-level operations, while `SensorReadingResource` handles all reading history logic in isolation. In a large API, combining every nested path into a single resource class would create a bloated controller that is difficult to read, test, and extend. Delegating nested paths to dedicated classes makes each class independently testable and allows the reading logic to evolve without touching the sensor resource.

### 4.2 Historical Data Management

`SensorReadingResource` provides `GET /` to retrieve a sensor's full reading history and `POST /` to append a new reading. Both methods validate that the sensor exists before proceeding, returning a `404` if it does not.

When a new reading is successfully posted, the implementation immediately calls `s.setCurrentValue(reading.getValue())` on the parent `Sensor` object. This side effect ensures that the sensor's `currentValue` field always reflects the most recently recorded measurement, keeping the data consistent across both the sensor overview (`GET /api/v1/sensors`) and the reading history (`GET /api/v1/sensors/{id}/readings`). Without this update, a client requesting a sensor's current state could see a stale value that no longer matches the reading history.

---

## Part 5 — Advanced Error Handling, Exception Mapping and Logging

### 5.1 Resource Conflict, Dependency Validation, and State Constraints

The API uses three custom exceptions with dedicated mappers:

- **`RoomNotEmptyException` → 409 Conflict**: thrown when a DELETE is attempted on a room that still has sensors assigned.
- **`LinkedResourceNotFoundException` → 422 Unprocessable Entity**: thrown when a sensor is created with a `roomId` that does not exist.
- **`SensorUnavailableException` → 403 Forbidden**: thrown when a reading is posted to a sensor whose status is `"MAINTENANCE"`.

A **422 Unprocessable Entity** is more semantically accurate than **404 Not Found** for the missing-room-reference case because the problem is not that the request URI is wrong — the `/api/v1/sensors` endpoint exists and is reachable. The issue lies within the semantics of the request payload itself: the JSON body is syntactically valid but contains a `roomId` value that references a resource that does not exist. A `404` signals that the addressed resource could not be found, which would mislead a client into thinking the sensor endpoint itself is unavailable. A `422` correctly communicates that the server understood the request but was unable to process it due to a logical error in the submitted data.

### 5.2 Global Safety Net

`GlobalExceptionMapper` implements `ExceptionMapper<Throwable>`, making it a catch-all handler for any exception not handled by a more specific mapper. It intercepts unexpected runtime errors such as `NullPointerException` or `IndexOutOfBoundsException` and returns a clean `500 Internal Server Error` response with a generic JSON body.

From a **cybersecurity standpoint**, exposing raw Java stack traces is a significant risk because they reveal:

- **Package and class names** — attackers learn the internal structure and naming conventions of the application.
- **Method signatures and line numbers** — these confirm exactly where logic is implemented and which file to target.
- **Framework and library versions** — attackers can look up known vulnerabilities for those specific versions.
- **Internal data flow** — the call stack reveals how the application processes requests internally, which can expose logic flaws.

Returning a generic 500 message instead eliminates this information leakage, reducing the attack surface considerably.

### 5.3 Logging Filters

`LoggingFilter.java` implements both `ContainerRequestFilter` and `ContainerResponseFilter`. It uses `java.util.logging.Logger` to record the HTTP method and request URI on every incoming request, and the response status code on every outgoing response. The class is registered automatically by Jersey's package scan via the `@Provider` annotation.

Using a JAX-RS filter for logging is preferable to manually inserting `Logger.info()` calls inside every resource method because logging is a **cross-cutting concern** — it applies uniformly to all endpoints regardless of their business logic. Centralising this in a filter means:

- There is a single place to modify logging behaviour across the entire API.
- Resource methods remain clean and focused on their domain logic.
- New endpoints added in the future are automatically covered without any additional logging code.
- The approach is consistent with the separation of concerns principle and scales naturally as the API grows.

---

## Limitations

This project is intentionally constrained by the coursework requirements:

- All data is stored in memory and is lost when the server restarts
- No database or persistence layer is used
- No authentication or authorisation is implemented
- Input validation is limited to required-field presence checks
- Compound operations on the in-memory store are not fully atomic

---

## Conclusion

This project implements a fully functional Smart Campus RESTful API using JAX-RS and Jersey, covering resource modelling, nested sub-resources, query-based filtering, referential integrity validation, structured error handling through exception mappers, and cross-cutting logging via filters. The design adheres to RESTful principles and the coursework requirements while remaining readable and maintainable.

Created By,
Sasindi Linasha Korala
w2120469
