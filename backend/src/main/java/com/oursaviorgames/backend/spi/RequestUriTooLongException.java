package com.oursaviorgames.backend.spi;

import com.google.api.server.spi.ServiceException;

/**
 * HTTP 414 Request-URI Too Long.
 */
public class RequestUriTooLongException extends ServiceException {

    public RequestUriTooLongException( String statusMessage) {
        super(414, statusMessage);
    }
}
