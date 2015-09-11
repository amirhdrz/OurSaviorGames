package com.oursaviorgames.backend.auth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

import com.oursaviorgames.backend.utils.Preconditions;

/**
 * Google+ access token debugging url.
 */
class GPlusTokenDebugUrl extends GenericUrl {

    private static final String TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";

    /**
     * Query parameter 'access_token'
     * The access token to be inspected.
     */
    @SuppressWarnings("unused")
    @Key("access_token")
    private final String accessToken;

    /**
     * Constructor.
     * @param accessToken The access token to be inspected. Cannot be null.
     */
    private GPlusTokenDebugUrl(String accessToken) {
        super(TOKEN_INFO_URL);
        this.accessToken = Preconditions.checkNotNull(accessToken, "access token cannot be null");
    }

    /**
     * Builds an instance of {@link com.oursaviorgames.backend.auth.GPlusTokenDebugUrl}.
     * @param accessToken The access token to be inspected. Cannot be null.
     * @return
     */
    public static GPlusTokenDebugUrl buildUrl(String accessToken) {
        return new GPlusTokenDebugUrl(accessToken);
    }

}
