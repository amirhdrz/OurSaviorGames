package com.oursaviorgames.android.auth;

/**
 * Thrown if {@link com.oursaviorgames.android.auth.CredentialService.UserInfo} could not be retrieved.
 */
public class GetUserInfoException extends Exception {

    public GetUserInfoException() {
        super();
    }

    public GetUserInfoException(String detailMessage) {
        super(detailMessage);
    }

    public GetUserInfoException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public GetUserInfoException(Throwable throwable) {
        super(throwable);
    }
}
