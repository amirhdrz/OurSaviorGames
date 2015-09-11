package com.oursaviorgames.backend.auth.response;

import com.google.api.client.json.GenericJson;

/**
 * Represents an access token that has been debugged against one of the identity providers.
 */
abstract public class TokenDebugResponse extends GenericJson {

    /**
     * Returns the time at which this token expires
     * in unix epoch time format.
     * @return Expiry time.
     */
    abstract public long getExpiryTime();

    /**
     * Is this token valid.
     * @return True if valid, false otherwise.
     */
    abstract public boolean isValid();

    /**
     * The user id which this access token belongs to
     * as provided by the identity provider.
     * Helps verify the access token is valid for this user.
     * @return User id on the identity provider servers.
     */
    abstract public String getUserId();

    /**
     * The app this access token is associated with
     * as provided by the identity provider.
     * Helps verify the app id is for this app.
     * @return
     */
    abstract public String getAppId();

}
