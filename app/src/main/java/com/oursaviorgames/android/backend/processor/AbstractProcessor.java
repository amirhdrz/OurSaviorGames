package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.http.HttpStatus;

import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.util.LogUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public abstract class AbstractProcessor<T> implements Processor<T> {

    private static final String TAG = makeLogTag(AbstractProcessor.class);

    private boolean pending = true;
    private final Bundle requestParams;

    public AbstractProcessor(Bundle requestParams) {
        this.requestParams = requestParams;
    }

    @Override
    public final int run(HandlerService service) {
        int result = processRequest(service, requestParams);
        if (result != RS_CANCELLED) {
            if (result == RS_SUCCESS) {
                onSuccess(service);
            } else {
                onFailure(service, result);
            }
        }
        pending = false;
        return result;
    }

    @Override
    public T getResult() {
        if (pending) {
            throw new IllegalStateException("Processor has not been executed");
        }
        return onGetResult();
    }

    /**
     *
     * @param context
     * @param requestParams
     * @return One of RS_ result codes.
     */
    protected abstract int processRequest(HandlerService context, Bundle requestParams);

    /**
     * Called from {@link #run(HandlerService)} after
     * {@link #processRequest(HandlerService, android.os.Bundle)} returns
     * if the result code is {@link #RS_SUCCESS}.
     * @param context
     */
    protected void onSuccess(HandlerService context) {
        // subclasses should override to handle this event.
    }

    /**
     * Called from {@link #run(HandlerService)} after
     * {@link #processRequest(HandlerService, android.os.Bundle)} returns
     * if the result code is not {@link #RS_SUCCESS} or {@link #RS_CANCELLED}.
     * @param context
     * @param resultCode Result code returned from
     * {@link #processRequest(HandlerService, android.os.Bundle)}.
     */
    protected void onFailure(HandlerService context, int resultCode) {
        // subclasses should override to handle this event.
    }

    /**
     * Returns result after {@link #run(HandlerService)} has finished.
     * Guaranteed to be called after call to {@link #processRequest(HandlerService, Bundle)}.
     * <p>
     * Returns null by default.
     * @return result or null.
     */
    protected T onGetResult() {
        // subclasses should override to handle this event.
        return null;
    }


    /**
     * Converts HTTP status code to one of {@link Processor} result codes.
     * @param httpStatus Response HTTP status code.
     * @return One of {@link Processor} result codes or throws an {@link IllegalStateException}
     *             if a httpStatus codes was unexpected.
     */
    public static int convertHttpStatusCode(int httpStatus) {
        LOGD(TAG, "convertHttpStatusCode:: http code:" + httpStatus);
        //TODO: move this to processor, and handle all response codes, especially server busy and stuff
        switch (httpStatus) {

            // 200 OK
            case HttpStatus.SC_OK:
                return RS_SUCCESS;

            // 204 No Context
            case HttpStatus.SC_NO_CONTENT:
                return RS_SUCCESS;

            // 401 Unauthorized
            case HttpStatus.SC_UNAUTHORIZED:
                return RS_UNAUTHORIZED;

            // 403 Forbidden
            case HttpStatus.SC_FORBIDDEN:
                return RS_FORBIDDEN;

            // 409 Conflict
            case HttpStatus.SC_CONFLICT:
                return RS_CONFLICT_EXCEPTION;

            // 400 Bad Request
            case HttpStatus.SC_BAD_REQUEST:
                return RS_FAILED;

            // 404 Not found
            case HttpStatus.SC_NOT_FOUND:
                return RS_FAILED;

            default:
                Crashlytics.log(Log.ERROR, "Processor", "Unknown http code(" + httpStatus + ")");
                return RS_FAILED; //TODO: do not return failed. It could be potentially recoverable.
        }
    }

    /**
     * @param resultCode {@link Processor} result code.
     * @param msg Optional message to include, or null.
     * @return an instance of {@link ProcessorException}.
     */
    public static ProcessorException createExceptionFromResultCode(int resultCode, String msg) {
        switch (resultCode) {

            case RS_UNAUTHORIZED:
                return new UnauthorizedException(msg);

            case RS_CONFLICT_EXCEPTION:
                return new ConflictException(msg);

            case RS_IO_EXCEPTION:
                return new ProcessorIOException(msg);

            case RS_FORBIDDEN:
                return new ForbiddenException(msg);

            case RS_FAILED:
                return new ProcessorFailedException(msg);

            default:
                throw new IllegalArgumentException("ResultCode (" + resultCode + ") is invalid");
        }
    }

    @Override
    public String toString() {
        Bundle p = new Bundle(requestParams);
        p.remove(EndpointProcessor.PARAM_ACCESS_TOKEN); // do not expose access token to logs.
        return "[ processor:" + this.getClass().getSimpleName() + " , pending: " + pending + ", requestParams: " + requestParams.toString() + "]";
    }
}
