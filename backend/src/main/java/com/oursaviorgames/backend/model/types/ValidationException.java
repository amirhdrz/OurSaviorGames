package com.oursaviorgames.backend.model.types;

/**
 * Thrown when an argument fails sanity check by a subclass of {@link Validated}.
 */
public class ValidationException extends Exception {

    public ValidationException() {
        super();
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }

}
