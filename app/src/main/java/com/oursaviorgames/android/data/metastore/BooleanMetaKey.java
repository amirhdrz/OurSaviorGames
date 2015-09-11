package com.oursaviorgames.android.data.metastore;

import android.net.Uri;

public class BooleanMetaKey extends MetaKey<Boolean> {

    public BooleanMetaKey(Uri uri, String key, boolean defaultValue) {
        super(uri, key, defaultValue, Boolean.class);
    }

    @Override
    public Object getValueToPersist(Boolean value) {
        return value;
    }

    @Override
    public Boolean getValueFromPersisted(Object persistedValue) {
        return (Boolean) persistedValue;
    }
}
