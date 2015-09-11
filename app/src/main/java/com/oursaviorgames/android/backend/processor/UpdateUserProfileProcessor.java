package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.UserProfile;

import java.io.IOException;

import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.data.UserAccount;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class UpdateUserProfileProcessor extends EndpointProcessor<Void> {

    private static final String TAG = makeLogTag(UpdateUserProfileProcessor.class);

    public static final String PARAM_NEW_USERNAME = "new_username";

    private final String      newUsername;
    private       UserProfile mUserProfile;

    public UpdateUserProfileProcessor(Bundle reqParams) {
        super(reqParams);
        newUsername = reqParams.getString(PARAM_NEW_USERNAME);
        if (newUsername == null) {
            throw new IllegalArgumentException("newUsername cannot be null");
        }
    }

    @Override
    protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
        MobileApiEndpointRequest<UserProfile> request = endpoint.users().update(newUsername);
        mUserProfile = request.execute();
        return request;
    }

    @Override
    protected void onSuccess(HandlerService context) {
        if (mUserProfile != null) {
            LOGD(TAG, "updating UserProfile finished successfully");
            UserAccount.getUserAccount(context).updateUserAccount(mUserProfile.getUsername());
        }
    }

}
