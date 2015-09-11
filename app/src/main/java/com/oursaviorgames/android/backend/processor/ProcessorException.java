package com.oursaviorgames.android.backend.processor;

/**
 * A generic {@link Processor} exception.
 */
public class ProcessorException extends Exception {

    public ProcessorException() {
        super();
    }

    public ProcessorException(String detailMessage) {
        super(detailMessage);
    }

    public ProcessorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProcessorException(Throwable throwable) {
        super(throwable);
    }
}
