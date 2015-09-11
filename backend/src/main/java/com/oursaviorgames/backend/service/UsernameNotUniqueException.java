package com.oursaviorgames.backend.service;

/**
 * Indicates that the username is not unique and such a username
 * already exists in the database.
 */
public class UsernameNotUniqueException extends ServiceException {

    public UsernameNotUniqueException() {
        super();
    }

    public UsernameNotUniqueException(String message) {
        super(message);
    }

    public UsernameNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameNotUniqueException(Throwable cause) {
        super(cause);
    }
}
