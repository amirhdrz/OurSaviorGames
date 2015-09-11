package com.oursaviorgames.backend.auth;

/**
 * HTTP headers used for authorization.
 */
public class AuthorizationHeaders {

    /**
     * HTTP 'Authorization' header.
     * This header contains the access token provided to client by an identity provider.
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * HTTP 'X-IdentityProvider' header.
     * One of the registered {@link IdentityProvider}s.
     * The value is check to determine with which identity provider should the
     * access token be checked against.
     */
    public static final String IDENTITY_PROVIDER = "X-IdentityProvider";

    /**
     * HTTP 'X-UserId' header.
     * This is an optional header for authentication,
     * however if present authentication will become faster and cheaper to run.
     */
    public static final String USER_ID = "X-UserId";

}
