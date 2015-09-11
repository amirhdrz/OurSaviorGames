package com.oursaviorgames.android.ui;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.catwalk.CatWalkUIClient;
import com.oursaviorgames.android.catwalk.CatWalkView;
import com.oursaviorgames.android.catwalk.SecureResourceClient;
import com.oursaviorgames.android.data.GameProviderHelper;
import com.oursaviorgames.android.data.UserDataHelper;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.game.GameManifest;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.LogUtils;
import com.oursaviorgames.android.util.Utils;
import rx.Observable;
import rx.functions.Func2;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * XWalk fragment for games.
 */
public class GameFragment extends AuthedRoboHelperFragment {

    private static final String TAG = makeLogTag(GameFragment.class);

    public static final String ARG_GAME_DIR   = "game_dir";
    public static final String ARG_GAME_MODEL = "game_model";

    private File          mGameDir;
    private String        mGameUrl;
    private BaseGameModel mGameModel;
    private GameManifest  mManifest;
    private long          mPlayTimeStart;
    private long          mTotalPlayTime;

    /**
     * True while the user is playing the game. (Crosswalk timers are in resumed state).
     */
    private boolean mCurrentlyPlaying;

    /**
     * True if the user has played the game.
     * This implies Crosswalk timer's have been resumed before onStop is called.
     */
    private boolean mUserHasPlayed;

    @InjectView(R.id.catwalkView)
    CatWalkView mCatwalk;

    public static GameFragment createInstance(String gamePath, BaseGameModel model) {
        checkNotNull(gamePath, "Null gamePath");
        checkNotNull(model, "Null model");
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GAME_DIR, gamePath);
        args.putParcelable(ARG_GAME_MODEL, model);
        fragment.setArguments(args);
        return fragment;
    }

    public GameFragment() {
        // Required no-arg constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets manifest file pointed to by intent.
        final String gameDirPath = getArguments().getString(ARG_GAME_DIR);
        mGameModel = getArguments().getParcelable(ARG_GAME_MODEL);
        if (gameDirPath == null) {
            throw new IllegalStateException("Expected argument for gameDirPath " + ARG_GAME_DIR);
        }
        if (mGameModel == null) {
            throw new IllegalStateException("Expected argument for gameModel " + ARG_GAME_MODEL);
        }
        mGameDir = new File(gameDirPath);
        if (!mGameDir.exists() || !mGameDir.isDirectory()) {
            throw new IllegalArgumentException("Invalid game directory " + gameDirPath);
        }

        // Reads game manifest file.
        mManifest = readManifest(mGameDir);
        if (mManifest == null) {
            ErrorUtils.showFatalErrorDialog(getActivity(), R.string.error_game_load_fail);
        } else {

            mGameUrl = mManifest.getStartUrl();

            // Creates absolute file path if offline is true.
            if (mManifest.isOffline()) {
                mGameUrl = Uri.fromFile(mGameDir).buildUpon().appendPath(mGameUrl).build().toString();
            }

            // Set activity's orientation
            // Sets the orientation.
            switch(mManifest.getOrientation()) {
                case GameManifest.ORIENTATION_LANDSCAPE:
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                    break;
                case GameManifest.ORIENTATION_PORTRAIT:
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_game_xwalk, container, false);

        ButterKnife.inject(this, rootView);

        // Initializing XWalk.
        try {
            mCatwalk.setUIClient(new CatWalkUIClient(mCatwalk));
            mCatwalk.load(mGameUrl, null);
            mCatwalk.setResourceClient(new SecureResourceClient(getActivity(), mCatwalk, mGameDir));
        } catch (IOException e) {
            ErrorUtils.showFatalErrorDialog(getActivity(), R.string.error_game_load_fail);
            getActivity().finish();
        }
        return rootView;
    }

    /**
     * Returns Crosswalk's TextureView bitmap.
     */
    public Bitmap getCatWalkBitmap() {
        return mCatwalk.getBitmap();
    }

    /**
     * Resumes XWalk and javascript timers.
     * Also sets play time starting point.
     */
    public void resumeTimers() {
        LOGD(TAG, "resumeTimers");

        // Resume xwalk.
        mCatwalk.resumeTimers();
        mCatwalk.onShow();

        // Sets play start time
        mPlayTimeStart = System.currentTimeMillis();

        // The user is currently playing.
        mCurrentlyPlaying = true;
    }

    /**
     * Pauses XWalk and javascript timers.
     * Also pauses timer used for calculating play time.
     */
    public void pauseTimers() {
        LOGD(TAG, "pauseTimers");
        // Completely pause xwalk.
        mCatwalk.pauseTimers();
        mCatwalk.onHide();

        if (mCurrentlyPlaying) {
            // The user is no longer playing the game.
            mCurrentlyPlaying = false;
            mUserHasPlayed = true;

            long timeElapsed = System.currentTimeMillis() - mPlayTimeStart;
            mTotalPlayTime += timeElapsed;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mTotalPlayTime = 0l;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Convert total play time to seconds.
        int playTimeInSeconds = (int) (mTotalPlayTime / 1000l);
        LOGD(TAG, "onStop:: mUserHasPlayed: " + mUserHasPlayed + ", playTimeInSeconds: " + playTimeInSeconds);

        if (mUserHasPlayed) {
            mUserHasPlayed = false;
            // Update local PlayHistoryEntry.
            //TODO: subscription is holding a reference to this class.
            GameProviderHelper.updatePlayHistory(getActivity(), mGameModel.getGameId(), playTimeInSeconds)
                    .subscribe(new AndroidSubscriber<Uri>() {
                        @Override
                        public void onNext(Uri uri) {
                            LOGD(TAG, "playHistory updated");
                        }
                    });

            // only generate playtoken if threshold passed.
            if (mTotalPlayTime > (long) getResources().getInteger(R.integer.game_play_threshold_ms)) {
                //TODO: subscription is holding a reference to this class.
                final Context context = getActivity().getApplicationContext();
                Observable.combineLatest(getRoboHelper(),
                        UserDataHelper.insertPlayToken(context, mGameModel.getGameId(), playTimeInSeconds),
                        new Func2<RoboHelper, Uri, Void>() {
                            @Override
                            public Void call(RoboHelper roboHelper, Uri uri) {
                                LOGD(TAG, "calling to upload playtoken");
                                roboHelper.uploadPlayTokens();
                                return null;
                            }
                        }
                ).subscribe(new AndroidSubscriber<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        LOGD(TAG, "finished uploading playtoken");
                    }
                });
            }
        }

        mTotalPlayTime = 0l;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCatwalk.onDestroy();
        ButterKnife.reset(this);
    }

    /**
     * Opens game manifest from intent.
     * @param gameDir Game directory containing manifest file.
     * @return {@link GameManifest} or null if it could not be read.
     */
    public static GameManifest readManifest(File gameDir) {
        GameManifest manifest = null;
        try {
            String jsonStr = Utils.readFileFully(new File(gameDir, GameManifest.MANIFEST_FILE));
            manifest = new GameManifest(jsonStr);
        } catch (JSONException e) {
            if (LogUtils.DEBUG) e.printStackTrace();
            LOGE(TAG, "Manifest file is malformed");
        } catch (IOException e) {
            if (LogUtils.DEBUG) e.printStackTrace();
            LOGE(TAG, "Error reading game manifest file");
        }
        return manifest;
    }

}


