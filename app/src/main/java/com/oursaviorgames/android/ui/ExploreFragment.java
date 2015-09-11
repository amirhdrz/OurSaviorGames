package com.oursaviorgames.android.ui;

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

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.VideoProvider;
import com.oursaviorgames.android.ui.widget.ProgressBarSwitcher;
import com.oursaviorgames.android.ui.widget.SwitchButton;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class ExploreFragment extends MoonPlayerFragment
implements SwitchButton.SwitchListener,
           LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = makeLogTag(ExploreFragment.class);

    // Loader ids.
    private static final int TRENDING_LOADER_ID = 0;
    private static final int LATEST_LOADER_ID = 1;

    // Switch button, button ids.
    private static final int    TRENDING_SWITCH_ID = 0;
    private static final int    LATEST_SWITCH_ID   = 1;

    @InjectView(R.id.progressBarSwitcher)
    ProgressBarSwitcher mProgressBarSwitcher;
    @InjectView(R.id.gameList)
    ListView            mListView;
    @InjectView(R.id.sortOrderSwitch)
    SwitchButton        mSortOrderSwitch;

    int mCurrentLoader = -1;
    boolean isDatabaseUpdateFinished;
    VideoProvider   mVideoProvider;
    GameListAdapter mAdapter;

    Animation mFadeInAnimation;
    Animation mFadeOutAnimation;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExploreFragment.
     */
    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        return fragment;
    }

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get VideoProvider instance.
        mVideoProvider = VideoProvider.openProvider(getActivity());

        // Loads animations
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        mFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);

        // Update data.
        isDatabaseUpdateFinished = false;
        getRoboHelper()
                .subscribe(new Action1<RoboHelper>() {
                    @Override
                    public void call(RoboHelper roboHelper) {
                        updateData(roboHelper);
                    }
                });

        // Load Trending games.
        getLoaderManager().initLoader(TRENDING_LOADER_ID, null, this);
    }

    /**
     * Initializes adapter.
     * Should be called after mListView has been set.
     */
    private void initAdapter() {
        resetPlayer();
        mAdapter = new GameListAdapter(getActivity(), getMoonPlayer());
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Calling super implementation, ignoring the result.
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_explore, container, false);
        ButterKnife.inject(this, rootView);

        initAdapter();

        // Switch button
        // Set default active switch to Trending games..
        mSortOrderSwitch.setTextAndIds(getString(R.string.trending), getString(R.string.latest),
                TRENDING_SWITCH_ID, LATEST_SWITCH_ID, TRENDING_SWITCH_ID);
        mSortOrderSwitch.setOnSwitchListener(this);

        // Shows loading spinner.
        mProgressBarSwitcher.showSpinnerMultiple(1);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
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
        LOGD(TAG, "onSwitch with id " + activeSwitchId);
        switch (activeSwitchId) {
            case TRENDING_SWITCH_ID:
                // Destroys other loader if it has previously reported data.
                getLoaderManager().destroyLoader(LATEST_LOADER_ID);
                getLoaderManager().restartLoader(TRENDING_LOADER_ID, null, this);
                break;
            case LATEST_SWITCH_ID:
                // Destroys other loader if it has previously reported data.
                getLoaderManager().destroyLoader(TRENDING_LOADER_ID);
                getLoaderManager().restartLoader(LATEST_LOADER_ID, null, this);
                break;
        }
    }

    /**
     * Updates databases that are relevant to this fragment.
     * Should be binded to fragment lifecycle before use.
     */
    private void updateData(RoboHelper roboHelper) {
        addSubscription(bindObservable(Observable.combineLatest(roboHelper.updateGameList(GameContract.HotGameEntry.CONTENT_URI, false),
                roboHelper.updateGameList(GameContract.NewGameEntry.CONTENT_URI, false),
                new Func2<Void, Void, Void>() {
                    @Override
                    public Void call(Void aVoid, Void aVoid2) {
                        return null;
                    }
                }))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        LOGD(TAG, "Finished updating data");
                        isDatabaseUpdateFinished = true;
                        mProgressBarSwitcher.doneLoading();
                    }
                }));
    }

    /*
        -----------------
        Loader callbacks
        -----------------
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //TODO: use one loader id fro
        LOGD(TAG, "onCreateLoader:: id " + id);
        if (LATEST_LOADER_ID == id) {
            return new CursorLoader(getActivity(),
                    GameContract.NewGameListEntry.CONTENT_URI, null, null, null,
                    GameContract.NewGameListEntry.COLUMN_DATE_PUBLISHED + " DESC"
            );
        } else if (TRENDING_LOADER_ID == id) {
            return new CursorLoader(getActivity(),
                    GameContract.HotGameListEntry.CONTENT_URI, null, null, null,
                    GameContract.HotGameListEntry.COLUMN_HOT_SCORE + " DESC");
        } else {
            throw new IllegalArgumentException("Unknown id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LOGD(TAG, "onLoadFinished:: id " + loader.getId() + ", data size: " + data.getCount());

        final boolean isNewLoader = mCurrentLoader != loader.getId();
        mCurrentLoader = loader.getId();
        if (isNewLoader) {
            mListView.startAnimation(mFadeOutAnimation);
            initAdapter();
        }
        if (TRENDING_LOADER_ID == loader.getId()) {
            mAdapter.swapCursor(data, GameContract.HotGameListEntry.CONTENT_URI);
        } else if (LATEST_LOADER_ID == loader.getId()) {
            mAdapter.swapCursor(data, GameContract.NewGameListEntry.CONTENT_URI);
        }
        if (isNewLoader) {
            mListView.startAnimation(mFadeInAnimation);
        }
        mProgressBarSwitcher.doneLoading();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LOGD(TAG, "onLoaderReset:: id " + loader.getId());
        if (mCurrentLoader == loader.getId()) {
            mCurrentLoader = -1;
        }
        if (TRENDING_LOADER_ID == loader.getId()) {
            mAdapter.swapCursor(null, GameContract.HotGameListEntry.CONTENT_URI);
        } else if (LATEST_LOADER_ID == loader.getId()) {
            mAdapter.swapCursor(null, GameContract.NewGameListEntry.CONTENT_URI);
        }
    }
}
