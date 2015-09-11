package com.oursaviorgames.backend.service;

/**
 * Thrown when an invalid next page token is provided.
 */
public class InvalidPageTokenException extends ServiceException {

    public InvalidPageTokenException() {
        super();
    }

    public InvalidPageTokenException(String message) {
        super(message);
    }

    public InvalidPageTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPageTokenException(Throwable cause) {
        super(cause);
    }
}
