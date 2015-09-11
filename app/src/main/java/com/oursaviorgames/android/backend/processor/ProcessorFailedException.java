package com.oursaviorgames.android.backend.processor;

/**
 * Created by amir on 30/01/15.
 */
public class ProcessorFailedException extends ProcessorException {

    public ProcessorFailedException() {
        super();
    }

    public ProcessorFailedException(String detailMessage) {
        super(detailMessage);
    }

    public ProcessorFailedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProcessorFailedException(Throwable throwable) {
        super(throwable);
    }
}
