package com.oursaviorgames.android.moonplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.oursaviorgames.android.cache.VideoThumbnailCache;
import com.oursaviorgames.android.ui.widget.VideoView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Manages surfaces used by {@link MoonPlayer}.
 */
public class MoonVideoViewManager implements VideoView.VideoViewListener, View.OnAttachStateChangeListener{

    private static final String TAG = makeLogTag(MoonVideoViewManager.class);

    private final Context                mContext;
    private final SurfaceManagerListener mListener;
    private final Map<Integer, VideoView> mEntries = new HashMap<>(4); // Should not hold references to detached views.
    private final VideoThumbnailCache mVideoThumbnailCache;

    public MoonVideoViewManager(Context context, SurfaceManagerListener listener) {
        mContext = context;
        mListener = listener;
        mVideoThumbnailCache = VideoThumbnailCache.getInstance(context);
    }

    public void registerSurface(VideoView videoView,
                                String videoId, int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Id should be a positive integer. Got: " + id);
        }

        // If this is a recycled view, removes the old mapping.
        boolean recycled = false;
        Iterator<VideoView> it = mEntries.values().iterator();
        while (it.hasNext()) {
            VideoView v = it.next();
            if (v == videoView) {
                it.remove();
                recycled = true;
                break;
            }
        }
        if (!recycled) {
            videoView.addOnAttachStateChangeListener(this);
        }
        videoView.setMetaDataAndReset(id, videoId);
        videoView.setVideoViewListener(this);
        drawThumbnail(videoView);
        mEntries.put(id, videoView);
        LOGD(TAG, "registerSurface:: mEntries size: " + mEntries.size());
    }

    /**
     * If thumbnail is avaiable, draws it.
     * @param v
     */
    public void drawThumbnail(VideoView v) {
//        LOGE(TAG, "drawThumbnail:: Drawing thumbnail from SurfaceManager");
//        v.setThumbnail(mVideoThumbnailCache.getThumbnail(v.getVideoId()));
        drawThumbnailAsync(v);
    }

    //TODO: memcache is not thread-safe. This function should be changed in the future.
    public void drawThumbnailAsync(VideoView v) {
        final String videoId = v.getVideoId();
        final WeakReference<VideoView> mVideoViewRef = new WeakReference<>(v);
        Observable.just(mVideoThumbnailCache.getThumbnail(videoId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        VideoView videoView = mVideoViewRef.get();
                        if (videoView != null) {
                            if (TextUtils.equals(videoView.getVideoId(), videoId)) {
                                videoView.setThumbnail(bitmap);
                            }
                        }
                    }
                });
    }

    /**
     * Returns {@link VideoView} for this id,
     * of null if none is found.
     */
    public VideoView getVideoView(int id) {
        return mEntries.get(id);
    }

    /**
     * Convenience method for getting the video id associated with id.
     */
    public String getVideoId(int id) {
        VideoView v = mEntries.get(id);
        if (v != null) {
            return v.getVideoId();
        }
        return null;
    }

    /**
     * Returns Collection of all VideoViews.
     */
    public Collection<VideoView> getEntries() {
        return mEntries.values();
    }

    /**
     * Releases all resources held by this object.
     * @param cleanAndReleaseSurface Whether to clear Surfaces frame buffer.
     */
    public void purge(boolean cleanAndReleaseSurface) {
        if (cleanAndReleaseSurface) {
            for (VideoView videoView : mEntries.values()) {
                videoView.clearSurface();
                videoView.releaseSurface();
            }
        }
        mEntries.clear();
    }

    @Override
    public void onSurfaceTextureAvailable(VideoView videoView) {
        LOGE(TAG, "onSurfaceTextureAvailable:: position:" + videoView.getPosition() + ", entries:" + mEntries.size());

        LOGD(TAG, "onVideoViewAvailable:: calling drawThumbnail, entry: " + videoView.getPosition());
        mListener.onVideoViewAvailable(videoView);
    }

    @Override
    public void onSurfaceTextureDestroyed(VideoView videoView) {
        LOGE(TAG, "onSurfaceTextureDestroyed:: position:" + videoView.getPosition() + ", entries:" + mEntries.size());

        mListener.onVideoViewUnavailable(videoView);
    }

    @Override
    public void onVideoViewRecycled(VideoView videoView, int oldId, String oldVideoId) {
        mListener.onVideoViewRecycled(videoView, oldId, oldVideoId);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        LOGE(TAG, "onViewAttachedToWindow:: position:" + ((VideoView) v).getPosition() + ", entries:" + mEntries.size());
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        LOGE(TAG, "onViewDetachedFromWindow:: position:" + ((VideoView) v).getPosition() + ", entries:" + mEntries.size());
        // Removing references to detached views.
        mEntries.remove(((VideoView)v).getPosition());
    }

    public interface SurfaceManagerListener {

        /**
         * Called when videoView SurfaceTexture becomes available to draw onto.
         * @param videoView
         */
        void onVideoViewAvailable(VideoView videoView);

        /**
         * Called when videoView SurfaceTexture with given id is unavailable.
         * And should no longer be drawn on.
         * @param videoView
         */
        void onVideoViewUnavailable(VideoView videoView);

        /**
         * Called when the videoId for videoView has changed.
         * @param videoView recycled videoView.
         * @param oldId id of the videoView before recycling.
         * @param oldVideoId videoId of the videoView before recycling.
         */
        void onVideoViewRecycled(VideoView videoView, int oldId, String oldVideoId);
    }
}
