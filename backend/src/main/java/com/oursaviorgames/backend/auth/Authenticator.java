package com.oursaviorgames.backend.auth;

/**
 * Authenticator.
 */
public interface Authenticator {

    /**
     * Tries to authenticate user based on {@code authorization}.
     * User is authenticated if the access token provided is successfully debugged
     * against the identity provider, and returned user exists in the database.
     * @param authorization Authorization object parsed from client http request.
     * @return User object if user is authenticated, null otherwise.
     */
    public User authenticate(Authorization authorization);

}
