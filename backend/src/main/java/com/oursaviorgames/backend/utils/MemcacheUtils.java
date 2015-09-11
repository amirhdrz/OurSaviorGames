package com.oursaviorgames.backend.utils;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.logging.Level;

/**
 * Memcache utility functions.
 * {@link https://cloud.google.com/appengine/docs/java/memcache/}.
 */
public class MemcacheUtils {

    /**
     * Returns an instance of a synchronous memcache service with Log level {@link Level#INFO}.
     * @param namespace memcache namespace. Null for default memcache.
     * @return synchronous MemcacheService.
     */
    public static MemcacheService getSyncMemcacheWithLog(String namespace) {
        MemcacheService syncCache  = MemcacheServiceFactory.getMemcacheService(namespace);
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        return syncCache;
    }

}
