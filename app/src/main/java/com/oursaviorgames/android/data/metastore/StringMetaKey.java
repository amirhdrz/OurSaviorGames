package com.oursaviorgames.android.data.metastore;

import android.net.Uri;


public class StringMetaKey extends MetaKey<String> {

    private final String defaultValue;

    public StringMetaKey(Uri uri, String key, String defaultValue) {
        super(uri, key, defaultValue, String.class);
        this.defaultValue = defaultValue;
    }

    @Override
    public String getValueToPersist(String value) {
        return value;
    }

    @Override
    public String getValueFromPersisted(Object persistedValue) {
        return (String) persistedValue;
    }

}
