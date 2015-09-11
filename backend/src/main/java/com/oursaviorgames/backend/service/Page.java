package com.oursaviorgames.backend.service;

import java.util.Collection;

import com.oursaviorgames.backend.memcache.Cachable;

/**
 * Represents a page of results.
 */
public class Page<T> implements Cachable {

    private Collection<T> items;
    private String        token;

    public Page() {
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Collection<T> getItems() {
        return items;
    }

    public String getToken() {
        return token;
    }

}
