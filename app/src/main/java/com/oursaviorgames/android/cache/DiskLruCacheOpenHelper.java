package com.oursaviorgames.android.cache;

import android.os.ConditionVariable;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import com.oursaviorgames.android.external.disklrucache.DiskLruCache;

import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A helper class to manage {@link DiskLruCache} creation and version management.
 */
public class DiskLruCacheOpenHelper {

    private static final String TAG = makeLogTag(DiskLruCacheOpenHelper.class);

    private final ConditionVariable mInitCondition;
    private final int mCacheVersion;
    private final int mDiskCacheSize; // cache size in bytes.
    private final File mCacheDir;
    private DiskLruCache mDiskCache;

    public DiskLruCacheOpenHelper(File cacheDir, int cacheVersion, int diskCacheSize) {
        if (cacheVersion < 1) throw new IllegalArgumentException("Cache version must me >= 1");

        mCacheVersion = cacheVersion;
        mDiskCacheSize = diskCacheSize;
        mCacheDir = cacheDir;

        mInitCondition = new ConditionVariable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (DiskLruCacheOpenHelper.this) {
                    mInitCondition.open();
                    try {
                        mDiskCache = DiskLruCache.open(mCacheDir, mCacheVersion,1 , mDiskCacheSize);
                    } catch (IOException e) {
                        Crashlytics.logException(e);
                        throw new IllegalStateException("Could not open disk cache");
                    }
                }
            }
        }).start();
        mInitCondition.block();
    }

    /**
     * Returns File associated with key or null if it doesn't exist.
     * <p>
     * Fails silently if there was an error getting File from cache.
     * @return cached File or null if it doesn't exist or if there was an error.
     */
    public final synchronized File get(String key) {
        String safeKey = validateKey(key);
        try {
            return mDiskCache.getFile(safeKey);
        } catch (IOException e) {
            Crashlytics.logException(e);
            LOGE(TAG, "IOException reading file from cache");
            return null;
        }
    }

    /**
     * Returns an editor for the entry named key, or null if another edit is in progress.
     */
    public final synchronized DiskLruCache.Editor edit(String key) throws IOException {
        String safeKey = validateKey(key);
        return mDiskCache.edit(safeKey);
    }

    /**
     * Checks if key is valid according to LruCache
     * cache key pattern.
     * If not valid, return a hashes the cache key into a new key.
     */
    private String validateKey(String key) {
        if (key.length() >= 120) {
            // create a safe key.
            return String.valueOf(key.hashCode());
        } else {
            // key is safe
            return key;
        }
    }

    /**
     * Closes disk cache and clears cache directory.
     * <p>
     * This cache should not be used after this operation.
     */
    public final synchronized boolean clearCache() {
        try {
            if (!mDiskCache.isClosed()) {
                mDiskCache.close();
            }
            FileUtils.cleanDirectory(mCacheDir);
            return true;
        } catch (IOException e) {
            Crashlytics.logException(e);
            LOGE(TAG, "IOException cleaning cache directory");
            return false;
        }
    }

    /**
     * Closes disk cache.
     */
    public final synchronized void close() {
        try {
            if (!mDiskCache.isClosed()) {
                mDiskCache.close();
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
            LOGE(TAG,"IOException releasing disk cache");
        }
    }

}
