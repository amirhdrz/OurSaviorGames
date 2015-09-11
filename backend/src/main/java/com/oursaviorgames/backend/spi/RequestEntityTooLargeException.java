package com.oursaviorgames.backend.spi;

import com.google.api.server.spi.ServiceException;

/**
 * HTTP 413 Request-URI Too Long.
 */
public class RequestEntityTooLargeException extends ServiceException {

    public RequestEntityTooLargeException( String statusMessage) {
        super(413, statusMessage);
    }
}

