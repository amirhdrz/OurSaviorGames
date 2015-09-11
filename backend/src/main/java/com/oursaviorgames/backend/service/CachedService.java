package com.oursaviorgames.backend.service;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.logging.Level;

/**
 * A service with a named memcache.
 */
abstract class CachedService {

    /**
     * MemcacheService.
     */
    private MemcacheService memcache;

    /**
     * Initializes the MemcacheService.
     * @param cacheNameSpace Namespace of the memcache.
     */
    public CachedService(String cacheNameSpace) {
        memcache = MemcacheServiceFactory.getMemcacheService(cacheNameSpace);
        memcache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    }

    /**
     * Returns the MemcacheService.
     * @return MemcacheService.
     */
    public MemcacheService getMemcache() {
        return memcache;
    }

}
