package com.oursaviorgames.android.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.oursaviorgames.android.moonplayer.MoonPlayer;
import com.oursaviorgames.android.util.PrefUtils;

/**
 * An {@link AuthedRoboHelperFragment} that manages MoonPlayer.
 */
public class MoonPlayerFragment extends AuthedRoboHelperFragment
implements SharedPreferences.OnSharedPreferenceChangeListener {

    private MoonPlayer mPlayer;

    private void initPlayer() {
        mPlayer = new MoonPlayer(getActivity());
        mPlayer.setLooping(PrefUtils.getLoopVideo(getActivity()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPlayer();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onHidden() {
        super.onHidden();
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefUtils.getSharedPrefs(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PrefUtils.getSharedPrefs(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPlayer.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    /**
     * Returns this fragment's instance of {@link com.oursaviorgames.android.moonplayer.MoonPlayer}.
     */
    public MoonPlayer getMoonPlayer() {
        return mPlayer;
    }

    /**
     * Call this to reset the player.
     * All previously held resources are released,
     * and a new player is created.
     */
    public void resetPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
        }
        initPlayer();
        if (isUserVisible()) {
            mPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Has the looping setting changed?
        if (PrefUtils.PREF_LOOP_VIDEO.equals(key)) {
            mPlayer.setLooping(PrefUtils.getLoopVideo(getActivity()));
        }
    }
}
