package com.smartcampus.mapper;
import com.smartcampus.dto.ErrorResponse;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.*;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        // Rubric Section 5.1: Must use 422 for linked resource issues
        return Response.status(422).entity(new ErrorResponse(422, ex.getMessage())).build();
    }
}