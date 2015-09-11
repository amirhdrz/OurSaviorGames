package com.oursaviorgames.android.data.metastore;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

/**
 * A fast datastore.
 * Defined as singleton.
 */
public class MetaStore {

    private static final String SHARED_PREFS_NAME = "metastore";

    /**
     * Singleton
     */
    private static MetaStore sMetaStore;

    private SharedPreferences mSharedPrefs;

    /**
     * Private constructor.
     * @param context
     */
    private MetaStore(Context context) {
        mSharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns the singleton MetaStore.
     * @param context
     * @return
     */
    public static MetaStore getMetaStore(Context context) {
        if (sMetaStore == null) {
            sMetaStore = new MetaStore(context);
        }
        return sMetaStore;
    }

    /**
     * Puts {@code value} into the MetaStore.
     * @param metaKey
     * @param value
     * @param <T>
     */
    public <T> void putMeta(MetaKey<T> metaKey, T value) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        final Class persistType = metaKey.getPersistType();
        final Object valueToPersist = metaKey.getValueToPersist(value);
        final String key = metaKey.getKey();

        if (persistType == String.class) {
            editor.putString(key, (String) valueToPersist);
        } else if (persistType == Boolean.class) {
            editor.putBoolean(key, (Boolean) valueToPersist);
        } else if (persistType == Integer.class) {
            editor.putInt(key, (Integer) valueToPersist);
        } else if (persistType == Long.class) {
            editor.putLong(key, (Long) valueToPersist);
        } else if (persistType == Float.class) {
            editor.putFloat(key, (Long) valueToPersist);
        } else {
            throw new IllegalStateException("Datatype " + persistType.toString() + " is not supported");
        }

        editor.apply();
    }

    /**
     * Gets stored meta value from the MetaStore.
     * If not value has been previously stored, {@link MetaKey#getDefaultValue()}
     * is returned.
     * @param metaKey
     * @param <T>
     * @return
     */
    public <T> T getMeta(MetaKey<T> metaKey) {
        final Class persistType = metaKey.getPersistType();
        final String key = metaKey.getKey();

        if (mSharedPrefs.contains(key)) {
            if (persistType == String.class) {
                return metaKey.getValueFromPersisted(mSharedPrefs.getString(key, null));
            } else if (persistType == Boolean.class) {
                return metaKey.getValueFromPersisted(mSharedPrefs.getBoolean(key, false));
            } else if (persistType == Integer.class) {
                return metaKey.getValueFromPersisted(mSharedPrefs.getInt(key, 0));
            } else if (persistType == Long.class) {
                return metaKey.getValueFromPersisted(mSharedPrefs.getLong(key, 0l));
            } else if (persistType == Float.class) {
                return metaKey.getValueFromPersisted(mSharedPrefs.getFloat(key, 0.f));
            } else {
                throw new IllegalStateException("Datatype " + persistType.toString() + " is not supported");
            }
        } else {
            return metaKey.getDefaultValue();
        }
    }

    public static final String CLASS_CONTENT_AUTHORITY = "co.catalogg.android.data.ClassData";

    /** Builds Uri for classes that want to use {@link MetaStore}
     * but do not have a ContentProvider to back their data.
     * @param cls
     * @return
     */
    public static Uri buildClassUri(Class cls) {
        Uri uri = Uri.parse("content://" + CLASS_CONTENT_AUTHORITY);
        return uri.buildUpon().appendPath(cls.getSimpleName()).build();
    }

}
