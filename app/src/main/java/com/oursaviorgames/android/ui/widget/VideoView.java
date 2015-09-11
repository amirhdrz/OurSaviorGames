package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.external.materialishprogress.Spinner;

import java.lang.ref.WeakReference;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * This View's dimensions obey 16:9 aspect ratio.
 * Therefore this View ignores 'android:layout_height' property.
 */
public class VideoView extends FrameLayout implements TextureView.SurfaceTextureListener {

    private static final String TAG = makeLogTag(VideoView.class);

    // constants for msg.what for the TouchHandler below
    private static final int TOUCH_DOWN = 1;

    // Timeouts in milli-seconds fot TouchHandler below
    private static final int LONG_TOUCH_TIMEOUT = 200;

    private static final int FADE_IN_ANIMATION_DURATION = 200;
    private static final int FADE_OUT_ANIMATION_DURATION = 300;

    private static final float VIDEO_HIGHLIGHT_ALPHA = 0.5f;

    // touch feedback handler
    private static class TouchHandler extends Handler {
        final WeakReference<VideoView> videoViewRef;
        public TouchHandler(VideoView videoView) {
            videoViewRef = new WeakReference<>(videoView);
        }
        @Override
        public void handleMessage(Message msg) {
            if (TOUCH_DOWN == msg.what) {
                VideoView videoView = videoViewRef.get();
                if (videoView != null) {
                    videoView.mFadeInAnimation =videoView.mHighlightView.animate().alpha(VIDEO_HIGHLIGHT_ALPHA)
                            .setDuration(FADE_IN_ANIMATION_DURATION).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    VideoView videoView = videoViewRef.get();
                                    if (videoView != null) {
                                        videoView.mFadeInAnimation = null;
                                    }
                                }
                            });
                    videoView.mFadeInAnimation.start();
                }
            } else {
                throw new RuntimeException("Unknown message " + msg.what); // never
            }
        }
    }

    private final Spinner     mSpinner;
    private final TextureView mTextureView;
    private final View        mHighlightView;
    private final TouchHandler mHandler;

    private int  mId                      = -1;
    private long mCurrentPlaybackPosition = 0l;
    private String            mVideoId;
    private VideoViewListener mListener;
    private Drawable          mThumbnailDrawable;
    private boolean           isThumbnailDrawn;
    private Surface           mVideoSurface; // flag if video player is currently using the surface.
    private boolean mSurfaceLocked = false;

    private ViewPropertyAnimator mFadeInAnimation; // non-null while fade in animation is running.

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHandler = new TouchHandler(this);

        // Creates TextureView
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(this);

        // Creates Spinner
        mSpinner = new Spinner(context);
        mSpinner.setSize(Spinner.Size_large);
        mSpinner.setBarColor(context.getResources().getColor(R.color.whiteSpinnerColor));

        mHighlightView = new View(context);
        mHighlightView.setBackgroundColor(Color.BLACK);
        mHighlightView.setAlpha(0.0f);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        addView(mTextureView, lp);
        addView(mSpinner, lp);
        addView(mHighlightView, lp);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final boolean handled;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.sendEmptyMessageDelayed(TOUCH_DOWN, LONG_TOUCH_TIMEOUT);
                handled = true;
                break;
            case MotionEvent.ACTION_UP: {
                final boolean hasTouchMessage = mHandler.hasMessages(TOUCH_DOWN);
                if (hasTouchMessage) {
                    mHandler.removeMessages(TOUCH_DOWN);
                    mHighlightView.setAlpha(VIDEO_HIGHLIGHT_ALPHA);
                }
                mHighlightView.animate().alpha(0.f).setDuration(FADE_OUT_ANIMATION_DURATION).start();
                handled = true;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                final boolean hasTouchMessage = mHandler.hasMessages(TOUCH_DOWN);
                if (hasTouchMessage) {
                    mHandler.removeMessages(TOUCH_DOWN);
                }
                final boolean fadeInAnimationRunning = mFadeInAnimation != null;
                if (fadeInAnimationRunning) {
                    mFadeInAnimation.cancel();
                    // animation end action will not run if cancelled,
                    // Nullifying variable here.
                    mFadeInAnimation = null;
                }
                if (mHighlightView.getAlpha() > 0.f || fadeInAnimationRunning) {
                    mHighlightView.animate().alpha(0.f).setDuration(FADE_OUT_ANIMATION_DURATION).start();
                }
                handled = true;
                break;
            }
            default:
                handled = false;
                break;
        }
        return super.onTouchEvent(event) || handled;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        float height = w * (9.f/16.f);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Resets playback position and removes thumbnail.
     * {@link #isThumbnailDrawn()} would return false.
     * @param id
     * @param videoId
     */
    public void setMetaDataAndReset(int id, String videoId) {
        if (id < 0) {
            throw new IllegalArgumentException("Id should be greater than zero");
        }

        // If videoId haven't changed, we do not need to recycle.
        if (mId == id && TextUtils.equals(mVideoId, videoId)) {
            LOGE(TAG, "setMetaDataAndRest:: not changing anything id: " + id + ", videoId: " + videoId);
            return;
        }

        int oldId = mId;
        String oldVideoId = mVideoId;

        mId = id;
        mSurfaceLocked = false;
        mSpinner.setVisibility(VISIBLE);
        mVideoId = videoId;
        mCurrentPlaybackPosition = 0l;
        mThumbnailDrawable = null;
        resetVideoPlayerFlags();
        clearSurface();

        if (mListener != null && oldVideoId != null) {
            mListener.onVideoViewRecycled(this, oldId, oldVideoId);
        }

        LOGD(TAG, String.format("setMetaDataAndReset:: surface unlocked. newId: %d, oldId: %d, " +
                "videoId: %s, oldVideoId: %s", id, oldId, videoId, oldVideoId));
    }

    private void resetVideoPlayerFlags() {
        isThumbnailDrawn = false;
    }

    public void setCurrentPlaybackPosition(long position) {
        mCurrentPlaybackPosition = position;
    }

    public int getPosition() {
        if (mId == -1) {
            throw new IllegalStateException("No id is set");
        }
        return mId;
    }

    public long getCurrentPlaybackPosition() {
        return mCurrentPlaybackPosition;
    }

    public String getVideoId() {
        return mVideoId;
    }

    public boolean isSurfaceLocked() {
        return mSurfaceLocked;
    }

    public boolean isThumbnailDrawn() {
        return isThumbnailDrawn;
    }

    /**
     * Returns Surface object for this View's SurfaceTexture,
     * or null if SurfaceTexture is not available yet.
     * <p>
     * Should call {@link #releaseSurface()} when done with this surface.
     * @return Surface if available or null.
     *
     * @see {@link #isAvailable()}.
     */
    public Surface getSurface() {
        LOGD(TAG, "getSurface:: id:" + mId + ", locked? " + mSurfaceLocked);
        mSurfaceLocked = true;
        mSpinner.setVisibility(GONE);
        return mVideoSurface;
    }

    /**
     * Client should call this to release surface lock.
     */
    public void releaseSurface() {
        mSurfaceLocked = false;
    }

    /**
     * Sets thumbnail drawable.
     * If the SurfaceTexture is not available yet,
     * thumbnail is drawn when it becomes available.
     * @param thumbnail
     */
    public void setThumbnail(Bitmap thumbnail) {
        LOGD(TAG, "setThumbnail:: id:" + mId + ", isThumbnailDrawn: " + isThumbnailDrawn);
        if (thumbnail != null) {
            mThumbnailDrawable = new BitmapDrawable(getContext().getResources(), thumbnail);
        }
        drawThumbnail();
    }

    /**
     * Clears VideoView Surface.
     * Draws a black background.
     */
    public void clearSurface() {
        if (!mSurfaceLocked && mTextureView.isAvailable()) {
            try {
                LOGD(TAG, "clearSurface:: clearing surface");
                Surface surface = new Surface(mTextureView.getSurfaceTexture());
                Canvas canvas = surface.lockCanvas(null);
                canvas.drawRGB(0, 0, 0);
                surface.unlockCanvasAndPost(canvas);
                surface.release();
            } catch (IllegalArgumentException e) {
                LOGE(TAG, "clearSurface:: IllegalArgumentException: id: " + mId);
            }
        }
    }

    /** Drawn thumbnail if available, black screen otherwise. */
    private void drawThumbnail() {
        LOGD(TAG, "drawThumbnail:: id:" + mId + ", isAvailable:" + mTextureView.isAvailable() + ", black?:" + (mThumbnailDrawable == null));
        if (!mSurfaceLocked && !isThumbnailDrawn && mTextureView.isAvailable()) {
            try {
                Surface surface = new Surface(mTextureView.getSurfaceTexture());
                Canvas canvas = surface.lockCanvas(null);
                // If not thumbnail was found,
                // just draw a black screen.
                if (mThumbnailDrawable != null) {
                    mThumbnailDrawable.setBounds(0, 0, getWidth(), getHeight());
                    mThumbnailDrawable.draw(canvas);
                    isThumbnailDrawn = true;
                    mSpinner.setVisibility(GONE);
                    LOGD(TAG, "...drawThumbnail:: id:" + mId + ", drawn, videoId:" + mVideoId);
                } else {
                    canvas.drawRGB(0, 0, 0);
                    mSpinner.setVisibility(VISIBLE);
                }
                surface.unlockCanvasAndPost(canvas);
                surface.release();
            } catch (IllegalArgumentException e) {
                LOGE(TAG, "...drawThumbnail:: IllegalArgumentException: id: " + mId);
            }
        }
    }

    /**
     * Sets the TextureView.SurfaceTextureListener used to listen to surface texture events.
     */
    public void setVideoViewListener(VideoViewListener listener) {
        mListener = listener;
    }

    /**
     * Returns true if the SurfaceTexture associated with this TextureView is available
     * for rendering. When this method returns true,
     * getSurfaceTexture() returns a valid surface texture.
     * @return
     */
    public boolean isAvailable() {
        return mTextureView.isAvailable();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LOGD(TAG, "onSurfaceTextureAvailable:: id:" + mId);
        clearSurface();

        if (!isThumbnailDrawn) {
            drawThumbnail();
        }

        mVideoSurface = new Surface(surface);
        if (mListener != null) {
            mListener.onSurfaceTextureAvailable(this);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // never called.
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
       if (mListener != null) {
            // Listener should guarantee that it will not draw to the surface after this call.
            mListener.onSurfaceTextureDestroyed(this);
        }

        resetVideoPlayerFlags();
        mVideoSurface.release();
        mVideoSurface = null;
        mSurfaceLocked = false;

        // Clearing surface before it is destroyed.
        clearSurface();

        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // ignore.
    }

    /**
     * Listener for VideoView's SurfaceTexture events.
     */
    public interface VideoViewListener {

        /**
         * videoView's SurfaceTexture is available for use.
         * @param videoView
         */
        public void onSurfaceTextureAvailable(VideoView videoView);

        /**
         * After this call returns no rending should be done inside this VideoView's
         * SurfaceTexture.
         */
        public void onSurfaceTextureDestroyed(VideoView videoView);

        /**
         * Called when this view is recycled.
         * @param videoView VideoView with new properties.
         * @param oldId id of the videoView before recycling.
         * @param oldVideoId videoId of the videoView before recycling.
         */
        public void onVideoViewRecycled(VideoView videoView, int oldId, String oldVideoId);
    }
}
