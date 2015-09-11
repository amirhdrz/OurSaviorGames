package com.oursaviorgames.backend.auth.response;

import com.google.api.client.util.Key;

/**
 * Facebook response from debugging an access token.
 * {@link https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow/v2.2#checktoken}.
 */
public final class FacebookTokenDebugResponse extends TokenDebugResponse {

    @Key("data")
    private ResponseData data;

    @Override
    public long getExpiryTime() {
        return data.expiresAt;
    }

    @Override
    public boolean isValid() {
        return data.isValid;
    }

    @Override
    public String getUserId() {
        return String.valueOf(data.userId);
    }

    @Override
    public String getAppId() {
        return String.valueOf(data.appId);
    }

    /** Facebook token debug 'data' field response. */
    public static class ResponseData {

        @Key("app_id")
        private long appId;

        @Key("expires_at")
        private long expiresAt;

        @Key("is_valid")
        private boolean isValid;

        @Key("issued_at")
        private long issuedAt;

        @Key("user_id")
        private long userId;

    }

}