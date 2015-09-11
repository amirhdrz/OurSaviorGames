package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.SimpleResponse;

import java.io.IOException;

import com.oursaviorgames.android.backend.HandlerService;


public final class GenerateUsernameEndpoint extends EndpointProcessor<String> {

    // Request parameter names
    public static final String PARAM_NAME = "name";

    private String mGeneratedUsername;

    public GenerateUsernameEndpoint(Bundle requestParams) {
        super(requestParams);
    }

    @Override
    protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle requestParams) throws IOException {
        final String name = requestParams.getString(PARAM_NAME);
        MobileApiEndpointRequest<SimpleResponse> request;
        request = endpoint.users().generateUsername(name);
        mGeneratedUsername = request.execute().getResult();
        return request;
    }

    @Override
    protected void onSuccess(HandlerService context) {
        // do nothing.
    }

    @Override
    protected void onFailure(HandlerService context, int resultCode) {
        // do nothing.
    }

    @Override
    protected String onGetResult() {
        return mGeneratedUsername;
    }

    @Override
    public String toString() {
        return GenerateUsernameEndpoint.class.getSimpleName() + " " + super.toString();
    }

}
