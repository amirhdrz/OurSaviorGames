package com.oursaviorgames.backend.auth;

import com.oursaviorgames.backend.auth.response.TokenDebugResponse;

/**
 * Represents an access token that has been validated against
 * the identity provider that originally issued it.
 */
public final class ValidatedToken {

    private final String accessToken;

    private final IdentityProvider identityProvider;

    private final TokenDebugResponse debugResponse;

    public ValidatedToken(String accessToken, IdentityProvider identityProvider, TokenDebugResponse debugResponse) {
        this.accessToken = accessToken;
        this.identityProvider = identityProvider;
        this.debugResponse = debugResponse;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    public TokenDebugResponse getDebugResponse() {
        return debugResponse;
    }

}
