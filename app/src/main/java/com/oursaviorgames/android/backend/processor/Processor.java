package com.oursaviorgames.android.backend.processor;

import android.os.Binder;

import com.oursaviorgames.android.backend.HandlerService;

/**
 * A Processor object is responsible for handling a REST Api call.
 * It performs the REST call and processes the response,
 * processing the response could meaning storing the data in the
 * local storage or providing a {@link Binder} interface.
 *
 */
public interface Processor<T> {

    /* List of result codes */
    //TODO: need to differential more between irrecoverable errors and recoverable ones
    // based on the http status code.

    /**
     * Processor has been cancelled and no change has been made to underlying data.
     */
    public static final int RS_CANCELLED = 0;

    /**
     * Processor has finished successfully.
     */
    public static final int RS_SUCCESS = 10;

    /**
     * Server replied with 401 Unauthorized.
     */
    public static final int RS_UNAUTHORIZED = 20;

    /**
     * Server replied with 409 Conflict.
     */
    public static final int RS_CONFLICT_EXCEPTION = 22;

    /**
     * There was an IO error when running this processor.
     */
    public static final int RS_IO_EXCEPTION  = 23;

    /**
     * Server replied with 403 Forbidden.
     */
    public static final int RS_FORBIDDEN = 30;

    /**
     * Processor failed for an unknown reason.
     */
    public static final int RS_FAILED = 31;

    /**
     * Executes and processes REST Api on a background thread.
     * @return One of RS_ result codes.
     */
    public int run(HandlerService service);

    /**
     * Returns result after run has finished.
     * Note that it is illegal to call this function before {@link #run(HandlerService)}
     * has returned.
     * @return Processor result.
     */
    public T getResult();

}
