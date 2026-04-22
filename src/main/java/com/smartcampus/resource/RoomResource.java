package com.smartcampus.resource;

import com.smartcampus.dto.ErrorResponse;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("/rooms")
public class RoomResource {

    private DataStore store = DataStore.getInstance();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Room> getRooms() {
        return new ArrayList<>(store.rooms.values());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room) {
        // Fix Bug #3: Validate required fields — ConcurrentHashMap throws NPE on null key
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Room 'id' field is required."))
                    .build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(400)
                    .entity(new ErrorResponse(400, "Room 'name' field is required."))
                    .build();
        }
        store.rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoom(@PathParam("id") String id) {
        // Fix Bug #1: Return 404 instead of 200 null
        Room r = store.rooms.get(id);
        if (r == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Room not found: " + id))
                    .build();
        }
        return Response.ok(r).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("id") String id) {
        // Fix Bug #2: Return 404 when room does not exist (remove() is a silent no-op)
        Room r = store.rooms.get(id);
        if (r == null) {
            return Response.status(404)
                    .entity(new ErrorResponse(404, "Room not found: " + id))
                    .build();
        }
        if (!r.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room with active sensors.");
        }
        store.rooms.remove(id);
        return Response.noContent().build();
    }
}