package com.northcoders.recordapi.exception;

public class AlbumAlreadyExistsException extends RuntimeException {
    public AlbumAlreadyExistsException(String message) {
        super(message);
    }
}
