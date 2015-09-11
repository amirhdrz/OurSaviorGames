package com.oursaviorgames.android.backend.processor;

/**
 * Created by amir on 31/01/15.
 */
public class ForbiddenException extends ProcessorException {

    public ForbiddenException() {
        super();
    }

    public ForbiddenException(String detailMessage) {
        super(detailMessage);
    }

    public ForbiddenException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ForbiddenException(Throwable throwable) {
        super(throwable);
    }
}
