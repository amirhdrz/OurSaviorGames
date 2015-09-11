package com.oursaviorgames.android.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.LruCache;

import java.io.File;

import com.oursaviorgames.android.data.VideoProvider;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Cache for video thumbnails.
 * Uses {@link VideoProvider} as source and holds an in-memory cache.
 */
//TODO: should we merge this with videoProvider, this is only a memory cache.
public class VideoThumbnailCache {

    private static final String TAG = makeLogTag(VideoThumbnailCache.class);

    private LruCache<String, Bitmap> cache;
    private VideoProvider mVideoProvider;

    private static VideoThumbnailCache sInstance;

    private VideoThumbnailCache(Context context) {
        mVideoProvider = VideoProvider.openProvider(context);
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024); // max available memory in KB.
        final int cacheSize = maxMemory / 8; // Using 1/8 of max memory for cache.
        cache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // Cache size is measured in kilo-bytes.
                return bitmap.getAllocationByteCount() / 1024;
            }
        };
        LOGD(TAG, "cacheSize:" + cacheSize);
    }

    /**
     * Returns global instance of this cache.
     * <p>
     * Should call {@link #clearInstanceAndPurgeCache()} when done with this cache.
     */
    public static VideoThumbnailCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VideoThumbnailCache(context);
        }
        return sInstance;
    }

    /**
     * Returns thumbnail for videoId.
     * <p>
     * Note if Video is not available in in-memory cache, disk is accessed.
     *
     * @return Thumbnail if cached (in memory, or disk) or null if it doesn't exist.
     */
    public Bitmap getThumbnail(String videoId) {
        Bitmap thumbnail = cache.get(videoId);
        if (thumbnail == null) {
            thumbnail = getFromDisk(videoId);
            if (thumbnail != null) {
                cache.put(videoId, thumbnail);
            }
        }
        return thumbnail;
    }

    /**
     * Reads thumbnail from disk cache.
     */
    private Bitmap getFromDisk(String videoId) {
        Bitmap thumbnail = null;
        Uri uri = mVideoProvider.getThumbnail(videoId);
        if (uri != null) {
            File imageFile = new File(uri.getPath());
            thumbnail = BitmapFactory.decodeFile(imageFile.getPath());
        }
        return thumbnail;
    }

    /**
     * Nullifies static instance and empties memory cache.
     */
    public static void clearInstanceAndPurgeCache() {
        if (sInstance != null) {
            sInstance.cache.evictAll();
            sInstance = null;
        }
    }

}
