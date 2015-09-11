package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import com.oursaviorgames.android.backend.AuthorizationHeaders;
import com.oursaviorgames.android.backend.HandlerService;

/**
 * OkHttp Processor with authentication fields.
 */
public abstract class AuthedOkHttpProcessor<T> extends OkHttpProcessor<T> {

    /**
     * Request parameters keys.
     */
    public static final String PARAM_ACCESS_TOKEN             = "accessToken";
    public static final String PARAM_IDENTITY_PROVIDER_DOMAIN = "ipDomain";
    public static final String PARAM_USER_ID                  = "userId";

    public AuthedOkHttpProcessor(Bundle requestParams) {
        super(requestParams);
    }

    @Override
    protected int processRequest(HandlerService context, Bundle reqParams, OkHttpClient client) {
        // Creates request builder with authorization parameters set.
        Request.Builder authedBuilder = new Request.Builder()
                .header(AuthorizationHeaders.HEADER_AUTHORIZATRION,
                        "Bearer " + reqParams.getString(PARAM_ACCESS_TOKEN))
                .header(AuthorizationHeaders.HEADER_IDENTITY_PROVIDER,
                        reqParams.getString(PARAM_IDENTITY_PROVIDER_DOMAIN));

        final String userId = reqParams.getString(PARAM_USER_ID);
        if (userId != null) {
            authedBuilder.header(AuthorizationHeaders.HEADER_USER_ID, userId);
        }
        return processRequest(context, reqParams, client, authedBuilder);
    }

    /**
     * @param context
     * @param reqParams
     * @param client OkHttpClient to use for making http calls.
     * @return One of RS_ result codes.
     */
    protected abstract int processRequest(HandlerService context, Bundle reqParams, OkHttpClient client, Request.Builder authedBuilder);
}
