package com.oursaviorgames.android.backend.processor;

/**
 * Created by amir on 30/01/15.
 */
public class ConflictException extends ProcessorException {

    public ConflictException() {
        super();
    }

    public ConflictException(String detailMessage) {
        super(detailMessage);
    }

    public ConflictException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConflictException(Throwable throwable) {
        super(throwable);
    }
}
