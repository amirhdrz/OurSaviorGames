package com.oursaviorgames.backend.http;

/**
 * Thrown when an HTTP header is invalid.
 */
public class HeaderInvalidException extends HttpHeaderException{

    public HeaderInvalidException() {
        super();
    }

    public HeaderInvalidException(String message) {
        super(message);
    }

    public HeaderInvalidException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeaderInvalidException(Throwable cause) {
        super(cause);
    }
}
