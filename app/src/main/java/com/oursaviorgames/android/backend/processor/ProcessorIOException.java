package com.oursaviorgames.android.backend.processor;

public class ProcessorIOException extends ProcessorException {

    public ProcessorIOException() {
        super();
    }

    public ProcessorIOException(String detailMessage) {
        super(detailMessage);
    }

    public ProcessorIOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProcessorIOException(Throwable throwable) {
        super(throwable);
    }
}
