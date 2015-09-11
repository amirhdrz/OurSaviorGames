package com.oursaviorgames.android.ui;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.crashlytics.android.Crashlytics;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.BackendResponseHelper;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.data.ExtraGamesHelper;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.VideoProvider;
import com.oursaviorgames.android.data.model.GameInfoModel;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.drawable.DiceDrawable;
import com.oursaviorgames.android.ui.widget.FloatingActionButton;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Home fragment.
 */
public class HomeFragment extends MoonPlayerFragment
implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = makeLogTag(HomeFragment.class);

    // Loader and Cursor ids.
    private static final int LAST_PLAYED_GAME_CURSOR_ID = 0;
    private static final int SPOTLIGHT_GAME_CURSOR_ID   = 1;

    // Views
    @InjectView(R.id.listView)
    ListView             mListView;
    @InjectView(R.id.emptyView)
    View                 mEmptyView;
    @InjectView(R.id.randomButton)
    FloatingActionButton mRandomButton;

    private VideoProvider           mVideoProvider;
    private HomeListAdapter         mAdapter;
    private OnScrollEventDispatcher mListViewOnScrollDispatcher;
    private HomeHandler             mHandler;
    private ContentObserver         mContentObserver;

    private boolean mPendingUpdateSpotlightGame;

    private boolean mDatabaseUpdateFinished = false;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new HomeHandler(this);
        mContentObserver = new MyContentObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(
                GameContract.PlayHistoryEntry.CONTENT_URI, true, mContentObserver);

        // Initialize adapter.
        mVideoProvider = VideoProvider.openProvider(getActivity());
        mAdapter = new HomeListAdapter(getActivity(), getMoonPlayer());

        updateDatabase(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, rootView);

        Drawable diceDrawable = new DiceDrawable(getActivity());
        mRandomButton.setDrawable(diceDrawable);

        // Initialize ListView on scroll event dispatcher.
        mListViewOnScrollDispatcher = new OnScrollEventDispatcher(mListView);
        mListViewOnScrollDispatcher.addListener(mAdapter);
        mListViewOnScrollDispatcher.addListener(mRandomButton);

        // set ListView adapter and scroll listener.
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyView);
        mListView.setFooterDividersEnabled(false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (mDatabaseUpdateFinished) {
//            //
//            LOGD(TAG, "onStart: Database update is already finished, restarting loaders");
//            initLoaders();
//        }
    }

    @Override
    public void onVisible() {
        super.onVisible();
        mRandomButton.setHidden(false);
    }

    @Override
    protected void onHidden() {
        super.onHidden();
        mRandomButton.setHidden(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Destroy the loaders.
        // Android really likes to call onLoaderFinished more than necessary.
//        getLoaderManager().destroyLoader(LAST_PLAYED_GAME_CURSOR_ID);
//        getLoaderManager().destroyLoader(SPOTLIGHT_GAME_CURSOR_ID);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
    }

    /*
            ---------------------
            View click listeners
            ---------------------
        */
    @OnClick(R.id.randomButton)
    public void onRandomClicked() {
        GameLoaderActivity.startActionRandomGame(getActivity());
   }

    /**
     * Updates databases that are relevant to this fragment.
     * Should be binded to fragment lifecycle before use.
     *
     * @param initLoaders Whether to initialize Loaders after database update has finished.
     */
    private void updateDatabase(final boolean initLoaders) {
        LOGD(TAG, "Starting to update database");
        mDatabaseUpdateFinished = false;
        addSubscription(bindObservable(
                getRoboHelper()
                        .flatMap(new Func1<RoboHelper, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(RoboHelper roboHelper) {
                                LOGD(TAG, "calling to update spotlight game");
                                return roboHelper.updateSpotlightGame(false);
                            }
                        }))
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        LOGD(TAG, "loadData unsubscribed");
                    }
                })
                .subscribe(new AndroidSubscriber<Void>() {
                    @Override
                    public void onNext(Void aVoid) {
                        LOGD(TAG, "Finished updating database");
                        mDatabaseUpdateFinished = true;
                        initLoaders();
                    }
                }));
    }

    private void initLoaders() {
        // Loading games.
        getLoaderManager().restartLoader(LAST_PLAYED_GAME_CURSOR_ID, null, HomeFragment.this);
        getLoaderManager().restartLoader(SPOTLIGHT_GAME_CURSOR_ID, null, HomeFragment.this);
    }

    // Creates GameInfoModel based on Cursor containing columns from BaseGameColumns.
    private GameInfoModel buildModelFromCursor(Uri contentUri, Cursor cursor) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, cv);
                return GameInfoModel.createFromContentValues(contentUri, cv);
            }
        }
        return null;
    }

    // Emits GameInfoModel for gameId or null.
    private Observable<GameInfoModel> getGameInfoModel(final String gameId, final boolean isSaved) {
        return bindObservable(getRoboHelper()
                .flatMap(new Func1<RoboHelper, Observable<GameCollectionResponse>>() {
                    @Override
                    public Observable<GameCollectionResponse> call(RoboHelper roboHelper) {
                        ArrayList<String> gameIds = new ArrayList<>(1);
                        gameIds.add(gameId);
                        return roboHelper.getGames(gameIds);
                    }
                })
                .map(new Func1<GameCollectionResponse, GameInfoModel>() {
                    @Override
                    public GameInfoModel call(GameCollectionResponse gameCollectionResponse) {
                        if (gameCollectionResponse != null && gameCollectionResponse.getItems() != null) {
                            GameResponse game = gameCollectionResponse.getItems().get(0);
                            ContentValues cv = new ContentValues();
                            BackendResponseHelper.gameResponseToContentValues(cv, game);
                            return new GameInfoModel(cv, true, isSaved);
                        }
                        return null;
                    }
                }));
    }

    /*
        ----------------
        Loader callbacks
        ----------------
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (LAST_PLAYED_GAME_CURSOR_ID == id) {
            LOGD(TAG, "Creating Loader LAST_PLAYED_GAME");
            // Loads last played game.
            return new CursorLoader(getActivity(),
                    GameContract.PlayHistoryListEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else if (SPOTLIGHT_GAME_CURSOR_ID == id){
            LOGD(TAG, "Creating Loader SPOTLIGHT_GAME");
            // Loads spotlight game.
            return new CursorLoader(getActivity(),
                    GameContract.ExtraGamesEntry.CONTENT_URI,
                    null,
                    GameContract.ExtraGamesEntry.COLUMN_KEY + " = '" + ExtraGamesHelper.SPOTLIGHT_GAME + "'",
                    null,
                    null);
        } else {
            throw new IllegalArgumentException("No loader exists with id (" + id + ")");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (LAST_PLAYED_GAME_CURSOR_ID == loader.getId()) {
            LOGD(TAG, "onLoadFinished:: last played game loaded");
            // Building gameModel from cursor data.
            if (data != null) {
                if (data.moveToFirst()) {
                    final String gameId = data.getString(data.getColumnIndex(GameContract.PlayHistoryListEntry.COLUMN_GAME_ID));
                    final boolean isSaved = !data.isNull(data.getColumnIndex(GameContract.PlayHistoryListEntry.COLUMN_SAVED_TABLE_ID_ALIAS));
                    addSubscription(
                            getGameInfoModel(gameId, isSaved)
                                    .subscribe(new AndroidSubscriber<GameInfoModel>() {
                                        @Override
                                        public void onNext(GameInfoModel gameInfoModel) {
                                            mAdapter.setResumedGame(gameInfoModel);
                                        }
                                    }));
                } else {
                    mAdapter.setResumedGame(null);
                }
            }
        } else if (SPOTLIGHT_GAME_CURSOR_ID == loader.getId()) {
            LOGD(TAG, "onLoadFinished:: Spotlight game loaded");
            final GameInfoModel model =
                    buildModelFromCursor(GameContract.ExtraGamesEntry.CONTENT_URI, data);
            if (model != null) {
                mAdapter.setSpotlightGame(model);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LOGD(TAG, "onLoaderReset:: loader id: " + loader.getId());
        mAdapter.notifyDataSetInvalidated();
    }

    /**
     * Handles async events on Main thread.
     */
    //TODO: generalize on this Handler for all fragments.
    private static class HomeHandler extends Handler {

        public static int UPDATE_SPOTLIGHT_GAME = 1;

        private WeakReference<HomeFragment> mFragmentRef;

        public HomeHandler(HomeFragment fragment) {
            mFragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (UPDATE_SPOTLIGHT_GAME == msg.what) {
                final HomeFragment fragment = mFragmentRef.get();
                if (fragment != null) {
                    // Calls to loadData() immediately if fragment
                    // is in a visible state.
                    // Otherwise sets the pending flag.
                    if (fragment.isResumed() && !fragment.isRemoving()) {
                        fragment.updateDatabase(false);
                    } else {
                        Crashlytics.log(Log.ERROR, "HomeFragment", "set mPendingUpdateSpotlightGame");
                    }
                }
            }
        }
    }

    // Listening for changes to last played game. Spotlight game may need to update.
    private static class MyContentObserver extends ContentObserver {

        private HomeHandler handler;

        public MyContentObserver(HomeHandler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            LOGD(TAG, "last played game changed");
            handler.sendMessage(handler.obtainMessage(HomeHandler.UPDATE_SPOTLIGHT_GAME));
        }
    }
}
