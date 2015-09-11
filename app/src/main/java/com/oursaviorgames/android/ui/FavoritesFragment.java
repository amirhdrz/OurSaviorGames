package com.oursaviorgames.android.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.GameProviderHelper;
import com.oursaviorgames.android.data.VideoProvider;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.widget.SwitchButton;
import com.oursaviorgames.android.util.PrefUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class FavoritesFragment extends MoonPlayerFragment implements SwitchButton.SwitchListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = makeLogTag(FavoritesFragment.class);

    // Loader ids.
    private static final int FAVORITES_LOADER_ID = 1;

    // Switch button, button ids.
    private static final String ACTIVE_SWITCH         = "activeSwitch";
    private static final int    LAST_PLAYED_SWITCH_ID = 0;
    private static final int    LAST_ADDED_SWITCH_ID  = 1;

    // Preferences
    private static final String PREF_SORT_ORDER = "sort_order";
    private static final int    PREF_SORT_ORDER_DEF = LAST_PLAYED_SWITCH_ID;

    @InjectView(R.id.gameList)
    ListView     mListView;
    @InjectView(R.id.sortOrderSwitch)
    SwitchButton mSortOrderSwitch;
    @InjectView(R.id.noCommentView)
    View         mEmptyView;
    @InjectView(R.id.switchButtonContainer)
    View         mSwitchButtonContainer;

    VideoProvider   mVideoProvider;
    GameListAdapter mAdapter;

    Animation mFadeInAnimation;
    Animation mFadeOutAnimation;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FavoritesFragment.
     */
    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();

        // Updates saved games.
        updateSavedGames();

        mVideoProvider = VideoProvider.openProvider(getActivity());

        // Initializes adapter
        mAdapter = new GameListAdapter(context, getMoonPlayer());

        // Loads animations
        mFadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        mFadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out);

        // Get persisted switch id.
        int persistedSwitchID = PrefUtils.getLocalPrefs(getActivity(), this.getClass())
                .getInt(PREF_SORT_ORDER, PREF_SORT_ORDER_DEF);

        // Create loaders
        Bundle b = new Bundle();
        b.putInt(ACTIVE_SWITCH, persistedSwitchID);
        getLoaderManager().initLoader(FAVORITES_LOADER_ID, b, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Calling super implementation, ignoring the result.
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.inject(this, rootView);

        updateEmptyStatus();

        // Get persisted switch id again, to set the active switch on mSortOrderSwitch
        int activeSwitchId = PrefUtils.getLocalPrefs(getActivity(), this.getClass())
                .getInt(PREF_SORT_ORDER, PREF_SORT_ORDER_DEF);
        mSortOrderSwitch.setTextAndIds(getString(R.string.last_played), getString(R.string.last_added),
                LAST_PLAYED_SWITCH_ID, LAST_ADDED_SWITCH_ID, activeSwitchId);
        mSortOrderSwitch.setOnSwitchListener(this);

        // Game ListView
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEmptyView(mEmptyView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /**
     * Updates the status of this fragment based on whether ListView adapter
     * is empty or not.
     * Only use after Views have been injected.
     */
    private void updateEmptyStatus() {
        if (mSwitchButtonContainer != null) {
            boolean empty = (mAdapter == null || mAdapter.getCount() == 0);
            if (empty) {
                mSwitchButtonContainer.setVisibility(View.GONE);
            } else {
                mSwitchButtonContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    // Updates base game columns of saved games table.
    private void updateSavedGames() {
        final Context context = getActivity().getApplicationContext();
        addSubscription(
            bindObservable(Observable.combineLatest(getRoboHelper(), GameProviderHelper.getSavedGamesIds(getActivity()), new Func2<RoboHelper, ArrayList<String>, Observable<GameCollectionResponse>>() {
                        @Override
                        public Observable<GameCollectionResponse> call(RoboHelper roboHelper, ArrayList<String> gameIds) {
                            return roboHelper.getGames(gameIds);
                        }
                    })
                    .flatMap(new Func1<Observable<GameCollectionResponse>, Observable<GameCollectionResponse>>() {
                        @Override
                        public Observable<GameCollectionResponse> call(Observable<GameCollectionResponse> gameCollectionResponseObservable) {
                            return gameCollectionResponseObservable;
                        }
                    })
                    .flatMap(new Func1<GameCollectionResponse, Observable<Void>>() {
                        @Override
                        public Observable<Void> call(GameCollectionResponse gameCollectionResponse) {
                            return GameProviderHelper.updateSavedGames(context, gameCollectionResponse);
                        }
                    }))
                    .subscribe(new AndroidSubscriber<Void>() {
                        @Override
                        public boolean onError2(Throwable e) {
                            if (e instanceof NoSuchElementException) {
                                // ignore. there no saved games to update.
                                return true;
                            }
                            return false;
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            LOGD(TAG, "finished updated saved games");
                        }
                    }));
    }


    /*
        --------------------
        View click listeners
        --------------------
     */

    /**
     * Called when the switch button is called.
     * @param activeSwitchId
     */
    @Override
    public void onSwitch(int activeSwitchId) {
        //TODO: init the loader and change the sorting order.
        LOGD(TAG, "onSwitch with id " + activeSwitchId);
        Bundle b = new Bundle();
        b.putInt(ACTIVE_SWITCH, activeSwitchId);
        getLoaderManager().restartLoader(FAVORITES_LOADER_ID, b, this);

        // Persist current switch position.
        SharedPreferences sp = PrefUtils.getLocalPrefs(getActivity(), this.getClass());
        sp.edit().putInt(PREF_SORT_ORDER, activeSwitchId).apply();
    }

    /*
        -----------------
        Loader callbacks
        -----------------
     */

    int mCurrentSwitchId = -1;
    boolean mSortSwitched = false;

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (FAVORITES_LOADER_ID == id) {

            if (!args.containsKey(ACTIVE_SWITCH)) {
                throw new IllegalStateException("Argument bundle should contain value for key ("
                        + ACTIVE_SWITCH + ")");
            }

            final int activeSwitch = args.getInt(ACTIVE_SWITCH);

            // Determines whether sort order has switched since last call.
            mSortSwitched = (activeSwitch != mCurrentSwitchId);
            mCurrentSwitchId = activeSwitch;

            final String sortOrder;
            if (LAST_PLAYED_SWITCH_ID == activeSwitch) {
                sortOrder = GameContract.SavedGameListEntry.COLUMN_LAST_PLAYED.desc().getSQL();
            } else if (LAST_ADDED_SWITCH_ID == activeSwitch) {
                sortOrder = GameContract.SavedGameListEntry.COLUMN_DATE_SAVED.desc().getSQL();
            } else {
                throw new IllegalStateException("Switch id (" + activeSwitch + ") is invalid");
            }

            return new CursorLoader(getActivity(),
                    GameContract.SavedGameListEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    sortOrder
            );

        } else {
            throw new IllegalArgumentException("No loader with id (" + id + ") exists");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LOGD(TAG, "loader finished");
        if (FAVORITES_LOADER_ID == loader.getId()) {
            if (mSortSwitched) {
                mListView.startAnimation(mFadeOutAnimation);
            }
            mAdapter.swapCursor(data, GameContract.SavedGameListEntry.CONTENT_URI);
            if (mSortSwitched) {
                mListView.setSelection(0);
                mListView.startAnimation(mFadeInAnimation);
            }
            // Reset the flag.
            mSortSwitched = false;
            updateEmptyStatus();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (FAVORITES_LOADER_ID == loader.getId()) {
            mAdapter.swapCursor(null, GameContract.SavedGameListEntry.CONTENT_URI);
            updateEmptyStatus();
        }
    }
}
