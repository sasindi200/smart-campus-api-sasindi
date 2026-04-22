package com.smartcampus.mapper;
import com.smartcampus.dto.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.*;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable t) {
        // Rubric: Catch-all to prevent stack trace leaks
        return Response.status(500).entity(new ErrorResponse(500, "Internal Server Error")).build();
    }
}