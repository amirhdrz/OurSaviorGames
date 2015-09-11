package com.oursaviorgames.android.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oursaviorgames.android.backend.BackendUrls;
import com.oursaviorgames.android.cache.DiskLruCacheOpenHelper;
import com.oursaviorgames.android.external.disklrucache.DiskLruCache;
import com.oursaviorgames.android.util.FileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

//TODO: should run in the context of a started service.
public class VideoProvider extends DiskLruCacheOpenHelper {

    private static final String TAG = makeLogTag(VideoProvider.class);

    // Cache prefix keys must be unique,
    // all cache files are stored in the same directory.
    private static final String VIDEO_CACHE_KEY_PREFIX     = "vid";
    private static final String THUMBNAIL_CACHE_KEY_PREFIX = "thumb";

    // Cache parameters.
    private static final int CACHE_VERSION   = 1;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 50; // 50MB

    // Map of pending video downloads.
    private Map<String, Observable<Uri>> mPendingObservables = new ConcurrentHashMap<>();

    // Global instance.
    private static VideoProvider sInstance;

    /**
     * VideoProvider only needs to be opened by the host Activity.
     */
    public static VideoProvider openProvider(Context context) {
        synchronized (VideoProvider.class) {
            if (sInstance == null) {
                sInstance = new VideoProvider(context);
            }
            return sInstance;
        }
    }

    /**
     * VideoProvider should be closed only by host Activity,
     * when the application is destroyed.
     */
    public static void closeOpenedProvider() {
        if (sInstance != null) {
            sInstance.close();
            sInstance = null;
        }
    }

    /**
     * Private constructor.
     * @param context
     */
    private VideoProvider(Context context) {
        super(FileUtils.getDir(context, FileUtils.DIR.VIDEOS), CACHE_VERSION, DISK_CACHE_SIZE);
    }

    /**
     * Checks cache before downloading video from network.
     * By default run on {@link rx.schedulers.Schedulers#io()} scheduler.
     * @param gameId
     * @return
     */
    public Observable<Uri> getVideo(final String gameId) {
        final String cacheKey = getVideoKey(gameId);
        return Observable.just(get(cacheKey))
                .flatMap(new Func1<File, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(File file) {
                        if (file == null) {
                            LOGD(TAG, "Cache miss (" + gameId + ")");
                            return downloadVideo(gameId);
                        } else {
                            LOGD(TAG, "Cache hit (" + gameId + ")");
                            // is there a better way to do this rather than returning an obervable.
                            return Observable.just(Uri.fromFile(file));
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Returns image thumbnail for a game.
     * @param gameId Game.
     * @return file Uri or null if no thumbnail is found in disk cache.
     */
    public Uri getThumbnail(String gameId) {
        File thumbnail = get(getThumbnailKey(gameId));
        return (thumbnail == null)? null : Uri.fromFile(thumbnail);
    }

    /**
     * Returns video cache key from game id
     * @param gameId
     * @return
     */
    private String getVideoKey(String gameId) {
        return VIDEO_CACHE_KEY_PREFIX + gameId;
    }

    /**
     * Returns thumbnail cache key from game id.
     * @param gameId
     * @return
     */
    private String getThumbnailKey(String gameId) {
        return THUMBNAIL_CACHE_KEY_PREFIX + gameId;
    }

    /**
     * Downloads video file from network and puts it into cache.
     * Creates a thumbnail from it and Returns the result from cache.
     * @param gameId
     * @return
     */
    private synchronized Observable<Uri> downloadVideo(String gameId) {
        LOGD(TAG, "DownloadVideo for gameId: " + gameId);
        if (!mPendingObservables.containsKey(gameId)) {
            LOGD(TAG, "No observable found. Creating one for gameId" + gameId);
            Observable<Uri> newObservable = createVideoDownloadObservable(gameId);
            mPendingObservables.put(gameId, newObservable);
        }
        return mPendingObservables.get(gameId);
    }

    /**
     * Creates an Observables that downloads video and creates thumbnail for {@code gameId}.
     * After observable completes, removes itself from mPendingObservables.
     * @param gameId
     * @return
     */
    private Observable<Uri> createVideoDownloadObservable(final String gameId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                LOGD(TAG, "Downloading video for game: " + gameId);
                final String cacheKey = getVideoKey(gameId);
                final MediaHttpDownloader downloader =
                        new MediaHttpDownloader(AndroidHttp.newCompatibleTransport(), null);
                DiskLruCache.Editor editor = null;
                OutputStream out = null;
                // Download video and put in cache.
                try {
                    editor = edit(cacheKey);
                    if (editor == null) {
                        // Return null if another edit is in progress.
                        // This should never happen.
                        subscriber.onError(new IllegalStateException("Another editor is in progress"));
                        return;
                    }
                    out = editor.newOutputStream(0);
                    GenericUrl downloadUrl = new GenericUrl(BackendUrls.getVideoDownloadUrl(gameId));
                    downloader.download(downloadUrl, out);
                    editor.commit();
                } catch (IOException e) {
                    // Abort edit.
                    if (editor != null) {
                        try {
                            editor.abort();
                        } catch (IOException e1) {
                            // ignore.
                        }
                    }
                    LOGE(TAG, "Error downloading video: " + e);
                    subscriber.onError(e);
                } finally {
                    // close the output stream.
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            //ignore.
                        }
                    }
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        })
        // retrieve the downloaded video from cache.
        .flatMap(new Func1<Void, Observable<Uri>>() {
            @Override
            public Observable<Uri> call(Void aVoid) {
                return Observable.just(Uri.fromFile(get(getVideoKey(gameId))));
            }
        })
        // create thumbnail
        .doOnNext(new Action1<Uri>() {
            @Override
            public void call(Uri uri) {
                createThumbnail(uri, gameId);
            }
        })
        // finally remove this pending download from mPendingObservables.
        .finallyDo(new Action0() {
            @Override
            public void call() {
                mPendingObservables.remove(gameId);
            }
        })
        .cache(1)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Creates thumbnail for given video uri and game id,
     * and saves to the same disk cache.
     * @param uri Video file uri.
     * @param gameId game id.
     */
    private void createThumbnail(Uri uri, String gameId) {
        LOGD(TAG, "Creating thumbnail for game : " + gameId);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri.getPath());
        Bitmap thumbnail = mmr.getFrameAtTime(0);
        mmr.release();
        // check if a thumbnail was found.
        if (thumbnail == null) {
            return;
        }
        // save thumbnail to file.
        DiskLruCache.Editor editor = null;
        OutputStream out = null;
        try {
            editor = edit(getThumbnailKey(gameId));
            if (editor == null) {
                // Return null if another edit is in progress.
                // This should never happen.
                throw new IllegalStateException("Another editor is in progress");
            }
            out = editor.newOutputStream(0);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);
            editor.commit();
        } catch (IOException e) {
            if (editor != null) {
                try {
                    editor.abort();
                } catch (IOException e1) {
                    // ignore.
                }
            }
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore.
                }
            }
        }
    }

}
