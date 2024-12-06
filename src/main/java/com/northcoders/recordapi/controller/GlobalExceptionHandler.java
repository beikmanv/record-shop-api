package com.northcoders.recordapi.controller;

import com.northcoders.recordapi.exception.AlbumAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlbumAlreadyExistsException.class)
    public ResponseEntity<Object> handleAlbumAlreadyExists(AlbumAlreadyExistsException ex) {
        // Create an error response with the message
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());

        // Return a 409 Conflict with the error message
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
}


