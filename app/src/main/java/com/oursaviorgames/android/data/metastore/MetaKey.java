package com.oursaviorgames.android.data.metastore;

import android.net.Uri;

/**
 * Represents a {@link MetaStore} meta key,
 * containing implementation of how to stringify and parse
 * values for this key.
 *
 * @param <T> Type of value associated with this meta key.
 */
public abstract class MetaKey<T> {

    private final Uri      uri;
    private final String   key;
    private final Class persistType;
    private final T defaultValue;

    /**
     * @param uri Cannot be null.
     * @param key Cannot be null.
     */
    public MetaKey(Uri uri, String key, T defaultValue, Class persistType) {
        this.uri = uri;
        this.key = key;
        this.defaultValue = defaultValue;
        this.persistType = persistType;
    }

    /**
     * @return Unique key to be used by {@link MetaStore}.
     */
    public final String getKey() {
        return uri.toString() + key;
    }

    public abstract Object getValueToPersist(T value);

    public abstract T getValueFromPersisted(Object persistedValue);

    public final Class getPersistType() {
        return persistType;
    }

    /**
     * The default value of this key.
     */
    public final T getDefaultValue() {
        return defaultValue;
    }

}
