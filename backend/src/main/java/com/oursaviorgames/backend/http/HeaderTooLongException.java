package com.oursaviorgames.backend.http;

/**
 * Thrown when a HTTP request header is much longer than expected.
 */
public class HeaderTooLongException extends HttpHeaderException {

    public HeaderTooLongException() {
        super();
    }

    public HeaderTooLongException(String message) {
        super(message);
    }

    public HeaderTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeaderTooLongException(Throwable cause) {
        super(cause);
    }
}
