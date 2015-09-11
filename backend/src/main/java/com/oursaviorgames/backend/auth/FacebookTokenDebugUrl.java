package com.oursaviorgames.backend.auth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;

import com.oursaviorgames.backend.Constants;
import com.oursaviorgames.backend.utils.Preconditions;

/**
 * Facebook access token debugging url.
 * {@link https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow/v2.2#checktoken}
 */
final class FacebookTokenDebugUrl extends GenericUrl {

    private static final String DEBUG_URL = "https://graph.facebook.com/debug_token";

    /**
     * Query parameter 'access_token'.
     * The app token.
     */
    @SuppressWarnings("unused")
    @Key("access_token")
    private final String APP_TOKEN = Constants.FACEBOOK_APP_ACCESS_TOKEN;

    /**
     * Query parameter 'input_token'
     * The access token to be inspected.
     */
    @SuppressWarnings("unused")
    @Key("input_token")
    private String inputToken;

    /**
     * Constructor.
     * @param inputToken The access token to be inspected. Cannot be null.
     */
    private FacebookTokenDebugUrl(String inputToken) {
        super(DEBUG_URL);
        this.inputToken = Preconditions.checkNotNull(inputToken, "input token cannot be null");
    }

    /**
     * Builds an instance of {@link com.oursaviorgames.backend.auth.FacebookTokenDebugUrl}.
     * @param inputToken The access token to be inspected. Cannot be null.
     * @return
     */
    public static FacebookTokenDebugUrl buildUrl(String inputToken) {
        return new FacebookTokenDebugUrl(inputToken);
    }

}
