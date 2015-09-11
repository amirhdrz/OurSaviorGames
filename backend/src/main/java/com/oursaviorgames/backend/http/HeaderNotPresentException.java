package com.oursaviorgames.backend.http;

/**
 * Thrown when a HTTP request does not contain a header that
 * was expected to be present in the header.
 */
public class HeaderNotPresentException extends HttpHeaderException {

    public HeaderNotPresentException() {
        super();
    }

    public HeaderNotPresentException(String message) {
        super(message);
    }

    public HeaderNotPresentException(String message, Throwable cause) {
        super(message, cause);
    }

    public HeaderNotPresentException(Throwable cause) {
        super(cause);
    }
}
