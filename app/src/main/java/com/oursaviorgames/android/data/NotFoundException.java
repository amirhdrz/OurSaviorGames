package com.oursaviorgames.android.data;

/**
 * Represents that an entity that was requested for was not found.
 */
public class NotFoundException extends Exception {

    public NotFoundException() {
        super();
    }

    public NotFoundException(String detailMessage) {
        super(detailMessage);
    }

    public NotFoundException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
