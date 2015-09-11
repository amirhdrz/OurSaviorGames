package com.oursaviorgames.android.backend.processor;

/**
 * Created by amir on 30/01/15.
 */
public class UnauthorizedException extends ProcessorException {

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String detailMessage) {
        super(detailMessage);
    }

    public UnauthorizedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnauthorizedException(Throwable throwable) {
        super(throwable);
    }
}
