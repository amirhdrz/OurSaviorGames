package com.oursaviorgames.android.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.game.GameLibrarian;
import com.oursaviorgames.android.game.GameTask;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

public class GameActivity extends AuthedActivity implements GameMenuFragment.GameMenuFragmentInterface {

    private static final String TAG = makeLogTag(GameActivity.class);

    public static final String EXTRA_GAME_DIR = "game_dir";
    public static final String EXTRA_GAME_MODEL = "game_model";

    private static final String TAG_GAME_FRAGMENT = "game";
    private static final String TAG_MENU_FRAGMENT = "menu";

    private BaseGameModel mGameModel;
    private Bitmap mCatWalkBitmap;
    boolean fromStopped = false;

    /**
     * Creates a fully formed Intent that can be used to start GameActivity.
     */
    public static Intent createIntent(Context context, File gameDir, BaseGameModel model) {
        checkNotNull(gameDir);
        checkNotNull(model);
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(EXTRA_GAME_DIR, gameDir.getPath());
        intent.putExtra(EXTRA_GAME_MODEL, model);
        return intent;
    }

    /**
     * on activity onCreate creates a {@link GameTask} to
     * get game from {@link GameLibrarian}.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Sets window mode.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        String gamePath = intent.getStringExtra(EXTRA_GAME_DIR);
        mGameModel = intent.getParcelableExtra(EXTRA_GAME_MODEL);

        if (savedInstanceState == null) {
            // Add game fragment.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, GameFragment.createInstance(gamePath, mGameModel), TAG_GAME_FRAGMENT)
                    .commit();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // If Activity is resuming from onStop and the menu
        // is not already showing, show the menu.
        if (fromStopped) {
            GameMenuFragment menuFragment = getMenuFragment();
            if (menuFragment == null) {
                GameFragment gameFragment = getGameFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, GameMenuFragment.createInstance(mGameModel, false), TAG_MENU_FRAGMENT)
                        .hide(gameFragment)
                        .commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If menu is already showing, don't do anything,
        // else resume the game.
        Fragment menuFragment = getMenuFragment();
        if (menuFragment == null) {
            LOGD(TAG, "onResume:: null menuFragment");
            getGameFragment().resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getGameFragment().pauseTimers();
        if (mCatWalkBitmap == null) {
            mCatWalkBitmap = getGameFragment().getCatWalkBitmap();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fromStopped = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fromStopped = false;
    }

    @Override
    public void onBackPressed() {
        // If menu is not shown, first display the menu.
        GameMenuFragment menuFragment = getMenuFragment();
        if (menuFragment == null) {
            GameFragment gameFragment = getGameFragment();
            // Pause game timers.
            gameFragment.pauseTimers();
            // Gets current crosswalk bitmap.
            mCatWalkBitmap = gameFragment.getCatWalkBitmap();
            // Shows menu fragment.
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, GameMenuFragment.createInstance(mGameModel, true), TAG_MENU_FRAGMENT)
                    .hide(gameFragment)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Bitmap getGameBackground() {
        return mCatWalkBitmap;
    }

    public void onUseInteraction(int interactionCode) {
        if (GameMenuFragment.GameMenuFragmentInterface.RESUME == interactionCode) {
            // Gets Fragments.
            GameFragment gameFragment = getGameFragment();
            GameMenuFragment menuFragment = getMenuFragment();

            // Remove menu fragment.
            getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .remove(menuFragment)
                    .show(gameFragment)
                    .commit();

            // Resume game.
            gameFragment.resumeTimers();

            // We don't need it anymore.
            mCatWalkBitmap = null;

        } else if (GameMenuFragment.GameMenuFragmentInterface.BACK == interactionCode) {
            // Finishes this Activity.
            finish();
        }
    }

    // Returns menu fragment from fragment manager.
    private GameMenuFragment getMenuFragment() {
        return (GameMenuFragment) getSupportFragmentManager().findFragmentByTag(TAG_MENU_FRAGMENT);
    }

    // Returns game fragment from fragment manager.
    private GameFragment getGameFragment() {
        return (GameFragment) getSupportFragmentManager().findFragmentByTag(TAG_GAME_FRAGMENT);
    }

}
