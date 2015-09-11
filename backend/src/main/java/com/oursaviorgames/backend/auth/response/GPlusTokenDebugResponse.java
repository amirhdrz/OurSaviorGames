package com.oursaviorgames.backend.auth.response;

import com.google.api.client.util.Key;

import com.oursaviorgames.backend.utils.TimeUtils;

/**
 * Google+ response from validation an access token.
 * {@link https://developers.google.com/accounts/docs/OAuth2UserAgent#validatetoken}.
 */
public final class GPlusTokenDebugResponse extends TokenDebugResponse {

    @Key("audience")
    private String audience;

    @Key("scope")
    private String scope;

    @Key("user_id")
    private String userId;

    @Key("expires_in")
    private long expiresIn;

    /**
     * Time at which this instance was created.
     */
    private long timeStamp = TimeUtils.getUnixTime();

    @Override
    public long getExpiryTime() {
        return expiresIn + timeStamp;
    }

    /**
     * Always returns true.
     * @return
     */
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getAppId() {
        return audience;
    }
}
