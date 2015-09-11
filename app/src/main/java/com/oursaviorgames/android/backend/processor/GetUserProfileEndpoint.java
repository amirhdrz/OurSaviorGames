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

public class GetUserProfileEndpoint extends EndpointProcessor<Boolean> {

    private static final String TAG = makeLogTag(GetUserProfileEndpoint.class);

    private UserProfile mUserProfile;

    private boolean response;

    public GetUserProfileEndpoint(Bundle reqParams) {
        super(reqParams);
    }

    @Override
    protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle requestParams) throws IOException {
        // creates and executes request.
        MobileApiEndpointRequest<UserProfile> request;
        request = endpoint.users().getProfile();
        mUserProfile = request.execute();
        return request;
    }

    @Override
    protected void onSuccess(HandlerService context) {
        // stores the result.
        if (mUserProfile != null) {
            final String userId = String.valueOf(mUserProfile.getId());
            final UserAccount.IdentityProvider ip = UserAccount.IdentityProvider.parseDomain(mUserProfile.getIdentityProviderDomain());
            UserAccount.getUserAccount(context).setUserSignedIn(
                    userId,
                    ip,
                    mUserProfile.getUsername(),
                    mUserProfile.getName(),
                    mUserProfile.getProfileImageUrl()
            );

            LOGD(TAG, "name: " + mUserProfile.getName());
            LOGD(TAG, "profile image: " + mUserProfile.getProfileImageUrl());

            response = true;
        } else {
            throw new IllegalStateException("Did not expect to get null UserProfile");
        }
    }

    @Override
    protected void onFailure(HandlerService context, int resultCode) {
        // Do nothing.
    }

    /**
     * Does not return a result.
     * @return
     */
    @Override
    protected Boolean onGetResult() {
        return response;
    }

    @Override
    public String toString() {
        return GetUserProfileEndpoint.class.getSimpleName() + " " + super.toString();
    }

}
