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

/**
 * Processor for UserSignUp endpoint.
 * On success, stores the result into {@link com.oursaviorgames.android.data.UserAccount}.
 */
public class UserSignupProcessor extends EndpointProcessor<Void> {

    private static final String TAG = makeLogTag(UserSignupProcessor.class);

    public static final String PARAM_NAME = "name";
    public static final String PARAM_USER_NAME = "username";
    public static final String PARAM_GENDER = "gender";
    public static final String PARAM_DEVICE_ID = "deviceId";

    private UserProfile mUserProfile;

    public UserSignupProcessor(Bundle reqParams) {
        super(reqParams);
    }

    @Override
    protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
        LOGD(TAG, "Sending request to signup user with params: " + reqParams.toString());
        final String name = reqParams.getString(PARAM_NAME);
        final String username = reqParams.getString(PARAM_USER_NAME);
        final String gender = reqParams.getString(PARAM_GENDER);
        final String deviceId = reqParams.getString(PARAM_DEVICE_ID);

        MobileApiEndpointRequest<UserProfile> request
                = endpoint.users().signup(deviceId, gender, name, username);
        mUserProfile = request.execute();
        LOGD(TAG, "UserSignupProcessor http response status: " + request.getLastStatusCode()
                + " " + request.getLastStatusMessage());
        return request;
    }

    @Override
    protected void onSuccess(HandlerService context) {
        // stores the result.
        if (mUserProfile != null) {
            LOGD(TAG, "User sign up run finished successfully");
            final String userId = String.valueOf(mUserProfile.getId());
            UserAccount.IdentityProvider ip = UserAccount.IdentityProvider.parseDomain(mUserProfile.getIdentityProviderDomain());
            UserAccount.getUserAccount(context).setUserSignedIn(userId, ip,
                    mUserProfile.getUsername(), mUserProfile.getName(), mUserProfile.getProfileImageUrl());

            LOGD(TAG, "profile image url: " + mUserProfile.getProfileImageUrl());
        } else {
            //TODO: will the returned entity ever be null?!?!??!
            //TODO: if yes, should we add it to other processors.
            throw new IllegalStateException("Did not expect to get null UserProfile");
        }
    }

    @Override
    protected void onFailure(HandlerService context, int resultCode) {
        // Do nothing.
    }

    @Override
    protected Void onGetResult() {
        return null;
    }

    @Override
    public String toString() {
        return UserSignupProcessor.class.getSimpleName() + " " + super.toString();
    }

}
