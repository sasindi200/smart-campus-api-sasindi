package com.smartcampus.resource;
import com.smartcampus.model.*;
import com.smartcampus.store.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/sensors")
public class SensorResource {
    private DataStore store = DataStore.getInstance();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSensor(Sensor s) {
        // Validate required fields
        if (s == null || s.getId() == null || s.getId().isBlank()) {
            return Response.status(400)
                    .entity(new com.smartcampus.dto.ErrorResponse(400, "Sensor 'id' field is required."))
                    .build();
        }
        if (!store.rooms.containsKey(s.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room does not exist.");
        }
        store.sensors.put(s.getId(), s);
        store.rooms.get(s.getRoomId()).getSensorIds().add(s.getId());
        return Response.status(201).entity(s).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null) return new ArrayList<>(store.sensors.values());
        return store.sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type)).toList();
    }

    @Path("/{id}/readings")
    public SensorReadingResource getReadings(@PathParam("id") String id) {
        return new SensorReadingResource(id);
    }
}