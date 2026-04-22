package com.smartcampus.resource;
import com.smartcampus.model.*;
import com.smartcampus.store.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

public class SensorReadingResource {
    private String sensorId;
    private DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) { this.sensorId = sensorId; }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postReading(SensorReading reading) {
        // Fix Bug #4: Null check before calling getStatus() — was causing NPE -> 500
        Sensor s = store.sensors.get(sensorId);
        if (s == null) {
            return Response.status(404)
                    .entity(new com.smartcampus.dto.ErrorResponse(404, "Sensor not found: " + sensorId))
                    .build();
        }
        if ("MAINTENANCE".equals(s.getStatus())) {
            throw new SensorUnavailableException("Sensor offline.");
        }
        s.setCurrentValue(reading.getValue());
        store.readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        return Response.status(201).entity(reading).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHistory() {
        // Fix Bug #5: Return 404 for unknown sensor instead of empty array
        if (!store.sensors.containsKey(sensorId)) {
            return Response.status(404)
                    .entity(new com.smartcampus.dto.ErrorResponse(404, "Sensor not found: " + sensorId))
                    .build();
        }
        return Response.ok(store.readings.getOrDefault(sensorId, new ArrayList<>())).build();
    }
}