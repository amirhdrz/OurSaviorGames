package com.oursaviorgames.backend.model.response;

import com.oursaviorgames.backend.memcache.Cachable;

/**
 * Used by a Collection Response that also takes tokens.
 * <p>
 * Note that we can't user generics with Google Cloud Endpoints due to type erasure.
 * but we can't use {@link com.google.api.server.spi.response.CollectionResponse},
 * since it can't be cached.
 */
public class TokenedResponse implements Cachable {

    private final String token;

    protected TokenedResponse(String token) {
        this.token = token;
    }

    public String getNextPageToken() {
        return token;
    }
}