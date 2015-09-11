package com.oursaviorgames.android.moonplayer;

import android.content.Context;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.FrameworkSampleSource;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.oursaviorgames.android.data.VideoProvider;
import com.oursaviorgames.android.ui.widget.VideoView;
import com.oursaviorgames.android.util.LogUtils;
import rx.Subscriber;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class MoonPlayer implements MoonVideoViewManager.SurfaceManagerListener, ExoPlayer.Listener {

    private static int    debugCount = 0; // instance count for debugging
    private        String TAG        = makeLogTag(MoonPlayer.class);

    private final Context              mContext;
    private final VideoProvider        mVideoProvider;
    private final ExoPlayer            mExoPlayer;
    private final MoonVideoViewManager mSurfaceManager;

    private PlayerHandler mHandler;
    private boolean              mLooping         = true;
    private Map<String, Boolean> mPrefetchMap     = new HashMap<>(4);
    private boolean              mPrefetchRunning = false;
    private MediaCodecVideoTrackRenderer mVideoRenderer; //TODO: make one global renderer

    int mRequestedPlayId = -1;
    private PlaybackBundle mCurrentBundle;
    private PlaybackBundle mPendingBundle;
    private WeakReference<VideoView> mVideoViewRef    = new WeakReference<>(null);
    private WeakReference<VideoView> mOldVideoViewRef = new WeakReference<>(null);

    private boolean mPlayWhenReady;

    public MoonPlayer(Context context) {
        if (LogUtils.DEBUG) {
            debugCount++;
            TAG += String.valueOf(debugCount);
        }
        mContext = context;
        mVideoProvider = VideoProvider.openProvider(context);
        mExoPlayer = ExoPlayer.Factory.newInstance(1);
        mExoPlayer.addListener(this);
        mSurfaceManager = new MoonVideoViewManager(context, this);
        mHandler = new PlayerHandler(this);
    }

    public void setLooping(boolean looping) {
        mLooping = looping;
    }

    /**
     * Releases all resources held by this class.
     * This instance of {@link MoonPlayer} should not be used
     * any longer.
     */
    public void release() {
        mSurfaceManager.purge(true);
        mExoPlayer.release();
    }

    /**
     * Starts playback if {@code playWhenReady}. If player is already
     * playing, {@code playWhenReady} can be used to pause or play the video.
     */
    public void setPlayWhenReady(boolean playWhenReady) {
        mPlayWhenReady = playWhenReady;
        mExoPlayer.setPlayWhenReady(playWhenReady);
        if (mRequestedPlayId >= 0 && !isPlayingId(mRequestedPlayId)) {
            LOGD(TAG, "setPlayWhenReady:: calling to prepare player. id:" + mRequestedPlayId);
            preparePlayer(mRequestedPlayId);
        }
    }

    /**
     * TODO: document this
     * @param id document what id is.
     * @param videoId
     * @param videoView
     */
    public void registerSurface(int id, String videoId, VideoView videoView) {
        LOGD(TAG, "registerSurface:: id: " + id + ", videoId: " +  videoId);
        if (id < 0) {
            throw new IllegalArgumentException("id should be a positive integer");
        }
        mSurfaceManager.registerSurface(videoView, videoId, id);

        // Prefetch video for the surface.
        final Collection<VideoView> views = mSurfaceManager.getEntries();
        Map<String, Boolean> newPrefetchMap = new HashMap<>(views.size());
        for (VideoView v : views) {
            final Boolean startedValue = mPrefetchMap.get(v.getVideoId());
            newPrefetchMap.put(v.getVideoId(), (startedValue == null) ? false : startedValue);
        }
        mPrefetchMap = newPrefetchMap;
        LOGD(TAG, "registerSurface:: mPrefetchMap: " + mPrefetchMap);
        handlePrefetch();
    }

    /**
     * Retrieves video for given id. Downloads it if necessary.
     * @param id
     */
    public void preparePlayer(final int id) {
        LOGD(TAG, "preparePlayer:: id: " + id);
        if (id < 0) throw new IllegalArgumentException("id should be >= 0");
        mRequestedPlayId = id;
        final String videoId = mSurfaceManager.getVideoId(id);
        if (videoId != null) {
            mVideoProvider.getVideo(videoId)
                    .subscribe(new Subscriber<Uri>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            LOGE(TAG, "preparePlayer:: error: " + e);
                            Crashlytics.logException(e);
                            if (e instanceof IllegalStateException) {
                                throw new IllegalStateException(e);
                            }
                        }

                        @Override
                        public void onNext(Uri uri) {
                            internalPreparePlayer(new PlaybackBundle(id, videoId, uri));
                        }
                    });
        }
    }

    /** Prepares ExoPlayer */
    private void internalPreparePlayer(PlaybackBundle pb) {
        LOGD(TAG, "internalPreparePlayer:: id: " + pb.id);
        if (mRequestedPlayId != pb.id) {
            LOGE(TAG, "internalPreparePlayer:: requested video changed. requested: " + mRequestedPlayId +
                    " got id: " + pb.id);
            return;
        }

        // Resets current play.
        mExoPlayer.setPlayWhenReady(false);
        mCurrentBundle = null;

        final VideoView videoView = mSurfaceManager.getVideoView(pb.id);
        if (videoView != null && videoView.isSurfaceLocked()) {
            LOGE(TAG, "...internalPreparePlayer:: surface locked videoView id:"
                    + videoView.getPosition()
                    + " videoId:" + videoView.getVideoId() + "...");
        }

        // Stops the player if not already stopped.
        if (ExoPlayer.STATE_IDLE != mExoPlayer.getPlaybackState()) {
            mExoPlayer.stop();
        }

        // Stores the current playback position
        VideoView oldVideoView = mVideoViewRef.get();
        if (oldVideoView != null) {
            oldVideoView.setCurrentPlaybackPosition(mExoPlayer.getCurrentPosition());
        }

        if ( !mPlayWhenReady || videoView == null || !videoView.isAvailable()) {
            if (!mExoPlayer.getPlayWhenReady()) {
                LOGD(TAG, "Player paused. id:" + pb.id);
            } else {
                LOGD(TAG, "...internalPreparePlayer:: videoView not available: id" +
                        ((videoView == null) ? null : videoView.getPosition()));
            }
            mPendingBundle = pb;
            return;
        }

        // Sets videoView transient state and
        // stores reference to oldVideoView to
        // unset it's transient state
        videoView.setHasTransientState(true);
        mOldVideoViewRef = mVideoViewRef;
        mVideoViewRef = new WeakReference<>(videoView);

        long playbackPosition = 0l;
        if (videoView.getVideoId().equals(pb.videoId)) {
            playbackPosition = videoView.getCurrentPlaybackPosition();
        }

        final SampleSource ss = new FrameworkSampleSource(mContext, pb.uri, null, 1);
        mVideoRenderer = new MediaCodecVideoTrackRenderer(ss,
                MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mExoPlayer.prepare(mVideoRenderer);
        LOGD(TAG, "...internalPreparePlayer:: setting new surface");
        mExoPlayer.sendMessage(mVideoRenderer,
                MediaCodecVideoTrackRenderer.MSG_SET_SURFACE,
                videoView.getSurface());
        mExoPlayer.seekTo(playbackPosition);

        mExoPlayer.setPlayWhenReady(true);
        mCurrentBundle = pb;
    }

    //TODO: better naming, write comment
    private void handlePrefetch() {
        if (!mPrefetchRunning) {
            prefetchNextVideo();
        }
    }

    /**
     * Handles prefetching of next video from {@code mPrefetchMap}
     * that hasn't been prefetched.
     */
    private void prefetchNextVideo() {
        // Checks if there are any videos that haven't been prefetched.
        String nextVideoId = null;
        for (String videoId : mPrefetchMap.keySet()) {
            if (!mPrefetchMap.get(videoId)) {
                mPrefetchMap.put(videoId, true);
                nextVideoId = videoId;
                break;
            }
        }
        if (nextVideoId == null) {
            mPrefetchRunning = false;
//            LOGD(TAG, "prefetchNextVideo:: stopped");

        } else {
            mPrefetchRunning = true;
            final String videoId = nextVideoId;
//            LOGD(TAG, "prefetchNextVideo:: id: " + nextVideoId);
            mVideoProvider.getVideo(nextVideoId)
                    .subscribe(new Subscriber<Uri>() {
                        @Override
                        public void onCompleted() {
                            //TODO: use a handler for this functionality.
                            prefetchNextVideo();
                        }

                        @Override
                        public void onError(Throwable e) {
                            LOGE(TAG, "preparePlayer error: " + e);
                            Crashlytics.logException(e);
                        }

                        @Override
                        public void onNext(Uri uri) {
                            // If no thumbnail is drawn for the current image
                            // attempt to draw it.
                            for (VideoView v : mSurfaceManager.getEntries()) {
                                if (v.getVideoId().equals(videoId)) {
                                    if (!v.isThumbnailDrawn()) {
//                                        LOGD(TAG, "prefetchNextVideo:: drawing thumbnail. id: " + v.getPosition());
                                        mSurfaceManager.drawThumbnail(v);
                                        break;
                                    }
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Stops the video player and purges registered {@link VideoView}s.
     */
    public void stop() {
        stopPlayerAndReleaseSurface(true);
    }

    /**
     * If player is not in {@link ExoPlayer#STATE_IDLE} state,
     * stops ExoPlayer and stores current playback position
     * and releases surface for current VideoView.
     * <p>
     * Sets {@code mCurrentBundle} to null.
     * @param unsetSurface if True blocks until surface is unset
     */
    private void stopPlayerAndReleaseSurface(boolean unsetSurface) {
        mCurrentBundle = null;
        if (mExoPlayer.getPlaybackState() != ExoPlayer.STATE_IDLE) {
            mExoPlayer.stop();
            if (unsetSurface) {
                mExoPlayer.blockingSendMessage(mVideoRenderer,
                        MediaCodecVideoTrackRenderer.MSG_SET_SURFACE, null);
            }
        }
        // Release previous surface if any.
        final VideoView vv = mVideoViewRef.get();
        if (vv != null) {
            vv.releaseSurface();
            vv.setHasTransientState(false);
        }
    }

    // Returns true if ExoPlayer is currently playing given id.
    private boolean isPlayingId(int id) {
        if (mCurrentBundle == null) return false;
        return (id == mCurrentBundle.id);
    }

    ////////////////////////////////////
    // Listeners
    ////////////////////////////////////
    @Override
    public void onVideoViewAvailable(VideoView videoView) {
        LOGD(TAG, "onVideoViewAvailable:: id: " + videoView.getPosition());
        // If current play id doesn't reflect the requested it,
        // and videoView for requested id is now available,
        // prepare player for the requested id.
        if (mPendingBundle != null
                && mPendingBundle.id == mRequestedPlayId
                && mPendingBundle.id == videoView.getPosition()
                && mPendingBundle.videoId.equals(videoView.getVideoId())) {

            LOGD(TAG, "onVideoViewAvailable:: playing pending bundle. id:" + videoView.getPosition());

            PlaybackBundle copy = mPendingBundle.clone();
            mPendingBundle = null;
            internalPreparePlayer(copy);
        } else if (!isPlayingId(mRequestedPlayId)) {
            // If VideoView becomes unavailable immediately after playing
            // we need to restart the player after it becomes available again
            // if the requested id hasn't changed.
            preparePlayer(mRequestedPlayId);
        }
   }

    @Override
    public void onVideoViewUnavailable(VideoView videoView) {
        // If we're currently playing video for given id,
        // stop ExoPlayer from drawing to that surface any longer.
        if (isPlayingId(videoView.getPosition())) {
            LOGD(TAG, "onVideoViewUnavailable:: id: " + videoView.getPosition());
            stopPlayerAndReleaseSurface(true);
        }
    }

    @Override
    public void onVideoViewRecycled(VideoView videoView, int oldId, String oldVideoId) {
        LOGD(TAG, "onVideoViewRecycled:: id: " + videoView.getPosition() + "...");
        // Keep a reference to mCurrentBundle,
        // calling to stop the player nullifies it.
        PlaybackBundle current = mCurrentBundle;

        if (mCurrentBundle != null
                && mCurrentBundle.id == oldId
                && mCurrentBundle.videoId.equals(oldVideoId)) {
            LOGD(TAG, "onVideoViewRecycled:: Stopping oldId:" + oldId);
            stopPlayerAndReleaseSurface(true);
        }

        // If this videoView has the requested play id,
        // prepare player to play requestedPlayId if
        // videoId has changed.
        if (mRequestedPlayId == videoView.getPosition()) {
            if (current == null || !current.videoId.equals(videoView.getVideoId())) {
                LOGD(TAG, "...onVideoViewRecycled:: restarting player:" + mRequestedPlayId);
                preparePlayer(mRequestedPlayId);
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mLooping && playbackState == ExoPlayer.STATE_ENDED) {
            // Loops the video.
            mExoPlayer.seekTo(0);
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        LOGE(TAG, "onPlayWhenReadyCommitted:: playWhenRead:" + mExoPlayer.getPlayWhenReady());

        VideoView view = mOldVideoViewRef.get();
        if (view != null) {
            view.setHasTransientState(false);
            LOGD(TAG, "onPlayWhenReadyCommitted:: removing transient state. id:"
                    + view.getPosition() + ", hasTransientState:" + view.hasTransientState());
        }

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        LOGE(TAG, "ExoPlayer error: " + error.toString());
        // Logging error with Crashlytics.
        Crashlytics.logException(error);

        if (LogUtils.DEBUG) {
            Throwable cause = error.getCause();
            if (cause instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
                MediaCodecTrackRenderer.DecoderInitializationException e = (MediaCodecTrackRenderer.DecoderInitializationException) cause;
                String decoderName = (e.decoderName == null) ? "" : e.decoderName;
                String diagnosticInfo = (e.diagnosticInfo == null) ? "" : e.diagnosticInfo;
                LOGE(TAG, "onPlayerError:: DecoderInitializationException : decodeName: " +
                        decoderName + ", diagnostic: " + diagnosticInfo);
            }
        }

        // Retrying the play request.
        if (mCurrentBundle != null) {
            // Releasing the current surface.
            stopPlayerAndReleaseSurface(true);
            // Call to re-prepare the player.
            preparePlayer(mRequestedPlayId);
        }
    }

    static class PlaybackBundle implements Cloneable{
        final int id;
        final String videoId;
        final Uri uri;

        PlaybackBundle(int id, String videoId, Uri uri) {
            this.id = id;
            this.videoId = videoId;
            this.uri = uri;
        }

        @Override
        protected PlaybackBundle clone() {
            try {
                return (PlaybackBundle) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("PlaybackBundle doesn't implement Cloneable");
            }
        }
    }

    private static class PlayerHandler extends Handler {

        private static final String TAG = makeLogTag(PlayerHandler.class);

        public static final int RE_PREPARE_PLAYER = 1;

        private WeakReference<MoonPlayer> mPlayerRef;

        public PlayerHandler(MoonPlayer moonPlayer) {
            mPlayerRef = new WeakReference<>(moonPlayer);
        }

        public void sendMessageRePreparePlayer(PlaybackBundle bundle) {
            Message message = obtainMessage();
            message.what = RE_PREPARE_PLAYER;
            if (bundle != null) {
                message.obj = bundle;
            } else {
                throw new IllegalArgumentException("Null bundle");
            }
            message.sendToTarget();
        }



        @Override
        public void handleMessage(Message msg) {
            final MoonPlayer moonPlayer = mPlayerRef.get();
            if (RE_PREPARE_PLAYER == msg.what) {
                if (moonPlayer != null) {
                    LOGD(TAG, "handleMessage:: re-preparing the player");
                    PlaybackBundle bundle = (PlaybackBundle) msg.obj;
                    moonPlayer.internalPreparePlayer(bundle);
                }
            } else {
                throw new IllegalArgumentException("Message.what " + msg.what + " is invalid");
            }
        }
    }

}
