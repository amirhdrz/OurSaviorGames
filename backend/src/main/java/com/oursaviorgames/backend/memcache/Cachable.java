package com.oursaviorgames.backend.memcache;

import java.io.Serializable;

/**
 * Represents an entity that can be cached by the memcache service.
 */
//TODO: all entities that are cached should implement this interface instead.
public interface Cachable extends Serializable {
}
