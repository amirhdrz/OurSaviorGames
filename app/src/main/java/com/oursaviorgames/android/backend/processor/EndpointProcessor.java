package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;
import android.text.TextUtils;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.google.api.client.http.HttpResponseException;

import java.io.IOException;

import com.oursaviorgames.android.backend.CloudEndpointsServiceBuilder;
import com.oursaviorgames.android.backend.HandlerService;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A processor that makes it easier to use Google Cloud Endpoints.
 */
abstract public class EndpointProcessor<T> extends AbstractProcessor<T> {

    private static final String TAG = makeLogTag(EndpointProcessor.class);

    /**
     * Request parameters keys.
     */
    public static final String PARAM_ACCESS_TOKEN             = "accessToken";
    public static final String PARAM_IDENTITY_PROVIDER_DOMAIN = "ipDomain";
    public static final String PARAM_USER_ID                  = "userId";

    // Keep a static reference to the endpoint, since it only needs to
    // be reinitialized if the access token changes.
    private static MobileApiEndpoint sEndpoint;
    private static String sAccessToken; // cached access token.

    public EndpointProcessor(Bundle reqParams) {
        super(reqParams);
        String accessToken = reqParams.getString(PARAM_ACCESS_TOKEN);
        // To build Cloud Endpoints service,
        // we only need to check if access token is changed,
        // since if identityProvider or userId change,
        // access token is guaranteed to change too.
        if (sEndpoint == null || !TextUtils.equals(sAccessToken, accessToken)) {
            sAccessToken = accessToken;
            String ip = reqParams.getString(PARAM_IDENTITY_PROVIDER_DOMAIN);
            String userId = reqParams.getString(PARAM_USER_ID);
            LOGD(TAG, "Creating endpoint with ip: " + ip + " and accessToken: " + accessToken);
            sEndpoint = CloudEndpointsServiceBuilder.buildApiEndpoint(sAccessToken, ip, userId);
        }
    }

    @Override
    protected final int processRequest(HandlerService context, Bundle requestParams) {
        try {
            MobileApiEndpointRequest request = processRequest(context, sEndpoint, requestParams);
            if (request == null) {
                return RS_SUCCESS;
            } else {
                if (request.getLastStatusCode() == -1) {
                    // request has not been executed.
                    throw new IllegalStateException("execute() method on MobileApiEndpointRequest should" +
                            " called before returning from processRequest()");
                }
                return convertHttpStatusCode(request.getLastStatusCode());
            }
        } catch (IOException e) {
            // if exception is an instance of HttpResponseException
            // it is not actually an IOException, therefore try to
            // get the http status code from the exception and return that.
            LOGD(TAG, "HttpResponseException: " + e.getMessage());
            if (e instanceof HttpResponseException) {
                int httpStatusCode = ((HttpResponseException) e).getStatusCode();
                return convertHttpStatusCode(httpStatusCode);
            } else {
                return RS_IO_EXCEPTION;
            }
        }
    }

    /**
     * Creates an instance of {@link MobileApiEndpointRequest} and executes the request.
     * Implementations can return null, if they don't want anything to happen,
     * however onSuccess will still be called and they're required to return appropriate result.
     * @param context Application's context.
     * @param endpoint Initialized MobileApiEndpoint.
     * @param reqParams Request parameters.
     * @return The instance of {@link MobileApiEndpointRequest} that is executed.
     * @throws IOException
     */
    abstract protected MobileApiEndpointRequest processRequest(HandlerService context,
                                          MobileApiEndpoint endpoint,
                                          Bundle reqParams)
    throws IOException;

    /** {@inheritDoc} */
    @Override
    protected void onSuccess(HandlerService context) {
        // subclasses should override to handle this event.
    }

    /** {@inheritDoc} */
    @Override
    protected void onFailure(HandlerService context, int resultCode) {
        // subclasses should override to handle this event.
    }

    /** {@inheritDoc} */
    @Override
    protected T onGetResult() {
        // subclasses should override to handle this event.
        return super.onGetResult();
    }

}
