package com.oursaviorgames.backend.http;

/**
 * Represents a generic exception with an Http header.
 * Should be subclassed for a more specific exception
 */
public class HttpHeaderException extends Exception {

    public HttpHeaderException() {
        super();
    }

    public HttpHeaderException(String message) {
        super(message);
    }

    public HttpHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpHeaderException(Throwable cause) {
        super(cause);
    }
}
