package com.oursaviorgames.android.data.metastore;

import android.net.Uri;

import java.util.Date;

/**
 * A {@link MetaKey} with {@link Date} value type.
 */
public class DateMetaKey extends MetaKey<Date>  {

    /**
     * @param uri Cannot be null.
     * @param key Cannot be null.
     * @param defaultDate Cannot be null.
     */
    public DateMetaKey(Uri uri, String key, Date defaultDate) {
        super(uri, key, defaultDate, Long.class);
    }

    @Override
    public Long getValueToPersist(Date value) {
        return value.getTime();
    }

    @Override
    public Date getValueFromPersisted(Object persistedValue) {
        return new Date((long) persistedValue);
    }

}
