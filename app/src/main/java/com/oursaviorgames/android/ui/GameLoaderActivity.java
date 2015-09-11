package com.oursaviorgames.android.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.crashlytics.android.Crashlytics;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.BackendResponseHelper;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.game.GameLibrarian;
import com.oursaviorgames.android.game.GameTask;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.LogUtils;
import com.oursaviorgames.android.util.Utils;

import io.fabric.sdk.android.services.common.Crash;
import rx.Observable;
import rx.functions.Func1;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

public class GameLoaderActivity extends AuthedActivity implements GameTask.GameTaskListener{

    private static final String TAG = makeLogTag(GameLoaderActivity.class);

    public final static String ACTION_LOAD_GAME = "load_game";
    public static final String ACTION_RANDOM_GAME = "random_game";

    // Extra intent parameters for ACTION_LOAD_GAME.
    public final static String EXTRA_GAME_MODEL     = "game_model";

    private GameLibrarian mGameLibrarian;
    private GameTask      mGameTask;
    private BaseGameModel mGameModel;

    /**
     * Activity starts game with given gameId.
     * <p>
     * Checks for internet availability before starting Activity.
     * @param context Context.
     * @param model Game id, cannot be null.
     */
    public static void startActionLoadGame(Context context, BaseGameModel model) {
        if (Utils.isNetworkAvailable(context)) {
            checkNotNull(model, "Null model");
            Intent intent = new Intent(context, GameLoaderActivity.class);
            intent.setAction(ACTION_LOAD_GAME);
            intent.putExtra(EXTRA_GAME_MODEL, model);
            context.startActivity(intent);
        } else {
            ErrorUtils.showOfflineError(context);
        }
    }

    /**
     * Start Activity and load a random game.
     * <p>
     * Checks for internet availability before starting Activity.
     */
    public static void startActionRandomGame(Context context) {
        if (Utils.isNetworkAvailable(context)) {
            Intent intent = new Intent(context, GameLoaderActivity.class);
            intent.setAction(ACTION_RANDOM_GAME);
            context.startActivity(intent);
        } else {
            ErrorUtils.showOfflineError(context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        try {
            mGameLibrarian = new GameLibrarian(this);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Could no open game librarian");
        }

        final Intent intent = getIntent();
        final String action = intent.getAction();
        // Checks intent action.
        if (ACTION_LOAD_GAME.equals(action)) {
            BaseGameModel model = getIntent().getParcelableExtra(EXTRA_GAME_MODEL);
            startGame(model);
        } else if (ACTION_RANDOM_GAME.equals(action)) {
            launchRandomGame();
        } else if (Intent.ACTION_VIEW.equals(action)) {
            try {
                Uri data = intent.getData();
                LOGD(TAG, data.toString());
                String gameId = Utils.decodeBase64String(data.getQueryParameter("g"));
                launchGame(gameId);
            } catch (NullPointerException | IllegalArgumentException e) {
                Crashlytics.logException(e);
                if (LogUtils.DEBUG) e.printStackTrace();
                ErrorUtils.showFatalErrorDialog(this, R.string.error_invalid_game_link);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGameTask != null) {
            mGameTask.cancel(true);
        }
        // This Activity can't remain in a paused state.
        if (!isFinishing()) {
            finish();
        }
    }

    // Helper method to get random game from server.
    private Observable<GameResponse> getRandomGame() {
        return getRoboHelper()
                .flatMap(new Func1<RoboHelper, Observable<GameResponse>>() {
                    @Override
                    public Observable<GameResponse> call(RoboHelper roboHelper) {
                        return roboHelper.getRandomGame();
                    }
                });
    }

    private Observable<GameResponse> getGame(final String gameId) {
        return getRoboHelper()
                .flatMap(new Func1<RoboHelper, Observable<GameCollectionResponse>>() {
                    @Override
                    public Observable<GameCollectionResponse> call(RoboHelper roboHelper) {
                        ArrayList<String> gameList = new ArrayList<>(1);
                        gameList.add(gameId);
                        return roboHelper.getGames(gameList);
                    }
                })
                .map(new Func1<GameCollectionResponse, GameResponse>() {
                    @Override
                    public GameResponse call(GameCollectionResponse gameCollectionResponse) {
                        if (gameCollectionResponse != null
                                && gameCollectionResponse.size() > 0) {
                            return gameCollectionResponse.getItems().get(0);
                        }
                        return null;
                    }
                });
    }

    private void launchGame(String gameId) {
        addSubscription(
                bindObservable(getGame(gameId))
                .subscribe(new AndroidSubscriber<GameResponse>() {
                    @Override
                    public void onNext(GameResponse gameResponse) {
                        startGame(BackendResponseHelper.gameResponseToGameModel(gameResponse));
                    }
                })
        );
    }

    private void launchRandomGame() {
        addSubscription(bindObservable(getRandomGame()).subscribe(new AndroidSubscriber<GameResponse>() {
            @Override
            public void onNext(GameResponse gameResponse) {
                startGame(BackendResponseHelper.gameResponseToGameModel(gameResponse));
            }
        }));
    }

    private void startGame(BaseGameModel model) {
        mGameModel = model;
        //TODO: flag_from_cache only so far
        mGameTask = mGameLibrarian.getGame(model.getGameId(), this);
    }

    @Override
    public void onGameTaskFinished(File gameDir, int status) {
        // Prepare and launch GameActivity.
        if (status == GameTask.STATUS_SUCCESS) {
            Intent intent = GameActivity.createIntent(this, gameDir, mGameModel);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {
            //TODO: go back to MainActivity if not already launched.
            ErrorUtils.showFatalErrorDialog(this, R.string.error_game_load_fail);
        }
    }

}

