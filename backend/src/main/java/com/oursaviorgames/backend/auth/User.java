package com.oursaviorgames.backend.auth;

import com.oursaviorgames.backend.memcache.Cachable;

/**
 * An immutable user object.
 * Represents minimal information about a user in our database.
 */
public final class User implements Cachable {

    private final long userId;

    public User(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }
}
