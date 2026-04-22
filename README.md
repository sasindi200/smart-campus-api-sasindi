# Smart Campus Sensor & Room Management API

## Overview

This project is a RESTful API developed for the **5COSC022W Client-Server Architectures coursework**. It models a simplified smart campus environment where rooms and sensors can be managed through a versioned JAX-RS web service.

The system allows clients to:

- create and retrieve rooms
- register sensors and assign them to rooms
- filter sensors by type
- record and retrieve historical sensor readings
- prevent invalid operations using business rules
- receive structured JSON error responses
- log incoming requests and outgoing responses

The API is built using **JAX-RS (Jakarta REST)**, packaged as a **WAR** file, and deployed on **Apache Tomcat**. In line with the coursework requirements, the project uses **in-memory data structures only** and does **not use any database**.

---

## API Design Summary

The API follows a resource-oriented design based on three main entities:

- **Room**
- **Sensor**
- **SensorReading**

### Resource hierarchy

- `/api/v1` → discovery endpoint
- `/api/v1/rooms` → room collection
- `/api/v1/rooms/{id}` → single room
- `/api/v1/sensors` → sensor collection
- `/api/v1/sensors/{sensorId}/readings` → readings for a specific sensor

This hierarchy reflects a clear RESTful structure. Rooms represent physical campus spaces, sensors belong to rooms, and sensor readings are nested under sensors as historical child resources.

---

## Technology Stack

- Java 17
- JAX-RS / Jakarta REST
- Jersey
- Apache Tomcat
- Maven
- NetBeans IDE

---

## Project Structure

```text
SmartCampusAPI/
├── pom.xml
├── README.md
├── nb-configuration.xml
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── smartcampus/
        │           ├── config/
        │           │   └── AppConfig.java
        │           ├── dto/
        │           │   └── ErrorResponse.java
        │           ├── exception/
        │           │   ├── LinkedResourceNotFoundException.java
        │           │   ├── RoomNotEmptyException.java
        │           │   └── SensorUnavailableException.java
        │           ├── filter/
        │           │   └── LoggingFilter.java
        │           ├── mapper/
        │           │   ├── GlobalExceptionMapper.java
        │           │   ├── LinkedResourceNotFoundExceptionMapper.java
        │           │   ├── RoomNotEmptyMapper.java
        │           │   └── SensorUnavailableExceptionMapper.java
        │           ├── model/
        │           │   ├── Room.java
        │           │   ├── Sensor.java
        │           │   └── SensorReading.java
        │           ├── resource/
        │           │   ├── DiscoveryResource.java
        │           │   ├── RoomResource.java
        │           │   ├── SensorResource.java
        │           │   └── SensorReadingResource.java
        │           └── store/
        │               └── DataStore.java
        └── webapp/
            ├── index.html
            ├── META-INF/
            │   └── context.xml
            └── WEB-INF/
                └── web.xml
```

### Package responsibilities

- **config**: API bootstrapping and versioned base path configuration
- **dto**: reusable response objects for structured error payloads
- **exception**: custom domain-specific exceptions
- **filter**: request and response logging
- **mapper**: exception-to-HTTP-response conversion
- **model**: POJO domain entities
- **resource**: REST endpoints and sub-resource logic
- **store**: in-memory data storage

---

## Core Data Models

### Room

Represents a physical room on campus.

Fields:

- `id`
- `name`
- `capacity`
- `sensorIds`

### Sensor

Represents a sensor device installed in a room.

Fields:

- `id`
- `type`
- `status`
- `currentValue`
- `roomId`

### SensorReading

Represents a historical reading recorded by a sensor.

Fields:

- `id`
- `timestamp`
- `value`

---

## How to Build and Run

### 1. Clone the repository

```bash
git clone https://github.com/sanularajapaksha-prog/smart-campus-api.git
cd smart-campus-api
```

### 2. Build the project with Maven

```bash
mvn clean package
```

### 3. Locate the generated WAR file

```text
target/SmartCampusAPI.war
```

### 4. Deploy the WAR file to Apache Tomcat

Copy the WAR file into the `webapps` folder of your Tomcat installation.

Example:

```text
C:\apache-tomcat\webapps\
```

### 5. Start Tomcat

Run:

```bash
startup.bat
```

### 6. Access the API

Base URL:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

---

## API Endpoints

### Discovery

- `GET /api/v1`

### Rooms

- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{id}`
- `DELETE /api/v1/rooms/{id}`

### Sensors

- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=Temperature`
- `POST /api/v1/sensors`

### Sensor Readings

- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

---

## Sample cURL Commands

### 1. Discovery endpoint

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a room

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"R1\",\"name\":\"Lab 1\",\"capacity\":40,\"sensorIds\":[]}"
```

### 3. Get all rooms

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 4. Create a sensor

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"S1\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"R1\"}"
```

### 5. Filter sensors by type

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature"
```

### 6. Add a sensor reading

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/S1/readings \
-H "Content-Type: application/json" \
-d "{\"id\":\"SR1\",\"timestamp\":1713945600000,\"value\":24.8}"
```

### 7. Get sensor reading history

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/S1/readings
```

### 8. Delete a room

```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/R1
```

---

## Error Handling

The API uses custom exceptions and exception mappers to ensure that clients receive structured JSON responses instead of raw server errors or stack traces.

### Implemented cases

- **409 Conflict**  
  Returned when attempting to delete a room that still has sensors assigned to it.

- **422 Unprocessable Entity**  
  Returned when creating a sensor with a `roomId` that does not exist.

- **403 Forbidden**  
  Returned when posting a reading to a sensor in `MAINTENANCE` status.

- **500 Internal Server Error**  
  Returned by a global exception mapper for unexpected runtime failures.

---

## Logging

A JAX-RS logging filter is used to capture:

- HTTP method
- request URI
- response status code

This improves observability and keeps logging separate from resource business logic.

---

# Conceptual Report

## Part 1 - Service Architecture and Setup

### 1.1 Project and Application Configuration

The application is configured as a Maven-based JAX-RS service using Jersey and deployed as a WAR file on Tomcat. The API entry point is versioned using `@ApplicationPath("/api/v1")`.

Although the coursework specification refers to subclassing `Application`, this project uses `ResourceConfig`, which is a Jersey-specific configuration class. It still serves the same purpose by registering the API and defining the versioned base path.

By default, JAX-RS resource classes are generally created on a per-request basis, meaning a new instance is used for each incoming request unless configured otherwise. This is helpful because it avoids storing shared mutable state inside resource classes themselves.

However, this project stores application data in a shared `DataStore` using in-memory collections. Because this shared state is accessed across multiple requests, concurrency becomes important. If several requests arrive at the same time, they may try to update the same data structures simultaneously. In a larger system, additional synchronization or thread-safe collections would be required to reduce the risk of race conditions and inconsistent updates.

### 1.2 Discovery Endpoint

The `GET /api/v1` endpoint acts as a discovery endpoint and returns useful API metadata such as version information, administrative contact details, and links to the main resources.

Hypermedia is considered an important feature of advanced RESTful design because it allows the server to guide the client through available resources and actions dynamically. Instead of relying only on static documentation, the client can inspect the response and follow links provided by the API itself.

This benefits client developers because it improves discoverability, reduces hard-coded assumptions about paths, and makes the API easier to evolve over time.

---

## Part 2 - Room Management

### 2.1 Room Resource Implementation

The room resource supports listing all rooms, creating new rooms, and retrieving a specific room by ID. These endpoints provide the core functionality required for room management.

When returning a list of rooms, returning only room IDs would reduce the response size and save bandwidth. This can be useful when the client only needs identifiers. However, it also means the client may need to make additional requests to get the full room details.

Returning full room objects increases payload size, but it is more convenient for clients because the relevant data is provided immediately. In this project, returning full room objects is reasonable because the data model is simple and clarity is more important than minimizing every byte transferred.

### 2.2 Room Deletion and Safety Logic

The API prevents deletion of a room if it still contains assigned sensors. This rule protects referential integrity and avoids leaving orphaned sensor records.

The DELETE operation is idempotent in effect because repeating the same DELETE request does not continue changing the system after the first successful deletion. Once the room has been removed, sending the same DELETE again does not produce additional changes to the final state.

Similarly, if a room cannot be deleted because it still contains sensors, repeated DELETE requests still leave the system unchanged because the room remains in place and the request is rejected each time.

---

## Part 3 - Sensor Operations and Linking

### 3.1 Sensor Resource and Integrity

The sensor resource validates the `roomId` in the request body when a new sensor is created. A sensor can only be registered if the referenced room already exists.

The annotation `@Consumes(MediaType.APPLICATION_JSON)` tells JAX-RS that the POST endpoint accepts JSON request bodies. If a client sends a different content type such as `text/plain` or `application/xml`, the runtime will not find a suitable method that can consume that format. In most cases, this results in a **415 Unsupported Media Type** response.

This is important because it enforces the API contract and ensures that the server only processes supported request formats.

### 3.2 Filtered Retrieval and Search

The sensors endpoint supports optional filtering by type using a query parameter, for example:

`GET /api/v1/sensors?type=CO2`

Using a query parameter is generally better than placing the filter inside the path such as `/api/v1/sensors/type/CO2` because query parameters are the standard way to express optional filtering and searching on a collection.

The resource is still the same collection of sensors; only the returned subset changes. Query parameters are also easier to extend later if more filters are needed, such as `status` or `roomId`.

---

## Part 4 - Deep Nesting with Sub-Resources

### 4.1 Sub-Resource Locator Pattern

The API uses a sub-resource locator for `/sensors/{sensorId}/readings`, which delegates that path to a dedicated `SensorReadingResource` class.

This improves the design because it separates responsibilities. The main sensor resource remains focused on sensor-level operations, while the sub-resource handles reading history. This makes the code more modular, readable, and maintainable.

In larger APIs, defining all nested routes in one large resource class would create unnecessary complexity. Separating nested responsibilities into dedicated classes makes the system easier to test, debug, and extend.

### 4.2 Historical Data Management

The sensor reading sub-resource supports:

- `GET` to retrieve all readings for a sensor
- `POST` to append a new reading

When a new reading is successfully added, the implementation also updates the `currentValue` field of the parent sensor. This keeps the current sensor state consistent with its reading history.

Without this update, the API could return a latest reading in the history that does not match the sensor’s reported current value.

---

## Part 5 - Advanced Error Handling, Exception Mapping and Logging

### 5.1 Resource Conflict, Dependency Validation and State Constraints

The API includes custom exceptions and exception mappers for business rule failures.

A **422 Unprocessable Entity** is more semantically accurate than **404 Not Found** when a client sends a valid JSON request body that contains a reference to a room that does not exist. In this case, the endpoint itself exists and the request syntax is valid, but the server cannot process the request because the linked resource reference is invalid.

A 404 is normally used when the URI itself does not match an existing resource. Here, the problem is inside the payload, so 422 is a more precise response.

### 5.2 Global Safety Net

The global exception mapper catches unexpected runtime errors and returns a generic 500 response rather than exposing internal Java details.

From a cybersecurity point of view, exposing raw stack traces is risky because they reveal internal implementation details such as:

- package and class names
- method names
- file names and line numbers
- framework details
- internal structure of the application

This information can help an attacker understand how the system is built and identify weak points. Returning a generic 500 response reduces information leakage and makes the API safer.

### 5.3 Logging Filters

Using JAX-RS filters for logging is better than manually placing `Logger.info()` statements inside every resource method because logging is a cross-cutting concern that affects the whole application.

This approach is better because it:

- avoids duplicate logging code
- keeps resource methods focused on business logic
- makes logging consistent across all endpoints
- simplifies maintenance
- scales better as more endpoints are added

---

## Limitations

This project is intentionally lightweight and follows coursework constraints, so it has some limitations:

- all data is stored in memory and is lost when the server restarts
- no database is used
- no authentication or authorization is implemented
- validation is basic
- concurrency handling is limited compared to a production-grade system

---

## Conclusion

This project demonstrates the implementation of a RESTful Smart Campus API using JAX-RS and in-memory storage. It covers resource modelling, nested resources, filtering, business-rule enforcement, exception mapping, and logging. The final design aims to satisfy the coursework requirements while also reflecting realistic REST API design principles.



