package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.GameProviderHelper;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.drawable.GrayscaleDrawable;
import rx.functions.Action1;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Menu fragment.
 */
public class GameMenuFragment extends BaseFragment {

    private static final String TAG = makeLogTag(GameMenuFragment.class);

    public static final String ARG_GAME_MODEL = "game_model";
    public static final String ARG_ANIMATE = "animate";

    private BaseGameModel mGameModel;
    private GrayscaleDrawable mBackgroundDrawable;
    private boolean mAnimate;

    @InjectView(R.id.container)
    FrameLayout mContainer;
    @InjectView(R.id.menuBox)
    View menuBox;
    @InjectView(R.id.favoriteButton)
    ImageView mFavoriteButton;

    public static GameMenuFragment createInstance(BaseGameModel model, boolean animate) {
        checkNotNull(model, "Null model");
        GameMenuFragment fragment = new GameMenuFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GAME_MODEL, model);
        args.putBoolean(ARG_ANIMATE, animate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof GameMenuFragmentInterface)) {
            throw new ClassCastException(activity.toString()
                    + " must implement UserInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGameModel = getArguments().getParcelable(ARG_GAME_MODEL);
        mAnimate = getArguments().getBoolean(ARG_ANIMATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_game_menu, container, false);
        ButterKnife.inject(this, rootView);

        // Sets pixelated background and starts animating.
        Bitmap catwalkBitmap = ((GameMenuFragmentInterface) getActivity()).getGameBackground();
        if (catwalkBitmap == null) {
            throw new IllegalStateException("Null game background");
        }
        mBackgroundDrawable = new GrayscaleDrawable(catwalkBitmap, 600);
        mContainer.setBackground(mBackgroundDrawable);

        if (mAnimate) {
            menuBox.setAlpha(0.f);
            menuBox.animate().alpha(1.0f).setDuration(200).start();
        }

        // Loads the state of the save button.
        addSubscription(bindObservable(GameProviderHelper.getSavedGame(getActivity(), mGameModel.getGameId()))
                .subscribe(new Action1<Uri>() {
                    @Override
                    public void call(Uri uri) {
                        boolean isFavorited = uri != null;
                        mFavoriteButton.setSelected(isFavorited);
                    }
                }));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAnimate) {
            mBackgroundDrawable.start();
        } else {
            mBackgroundDrawable.setSaturation(0.f);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // If the fragment is being removed from host Activity.
        // starts playing reverse pixelation animation.
        if (isRemoving()) {
            LOGD(TAG, "onPaused:: reverse animation");
            mBackgroundDrawable.reverse();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.favoriteButton)
    public void onFavoriteClicked(View view) {
        final Context context = getActivity();
        boolean isFavorited = !view.isSelected();
        view.setSelected(isFavorited);
        if (isFavorited) {
            GameProviderHelper.insertSavedGame(context, mGameModel)
                    .subscribe(new AndroidSubscriber<Uri>() {
                        @Override
                        public void onNext(Uri uri) {
                            //done.
                        }
                    });
        } else {
            // remove game from SavedGameEntry
            GameProviderHelper.deleteSavedGame(context, mGameModel.getGameId())
                    .subscribe(new AndroidSubscriber<Void>() {
                        @Override
                        public void onNext(Void aVoid) {
                            // done.
                        }
                    });
        }
    }

    @OnClick(R.id.backButton)
    public void onBackButtonClicked() {
        ((GameMenuFragmentInterface) getActivity()).onUseInteraction(GameMenuFragmentInterface.BACK);
    }

    @OnClick(R.id.resumeButton)
    public void onResumeButtonClicked() {
        ((GameMenuFragmentInterface) getActivity()).onUseInteraction(GameMenuFragmentInterface.RESUME);
    }

    /**
     * Hosting activity's should implement this interface.
     */
    public static interface GameMenuFragmentInterface {

        /**
         * Resume game button was clicked.
         */
        public static final int RESUME = 1;

        /**
         * Back button was clicked.
         */
        public static final int BACK = 2;


        /**
         * Called when the menu resume button is clicked.
         * @param interactionCode One of codes in this interface.
         */
        public void onUseInteraction(int interactionCode);

        /**
         * Hosting activity should return game background.
         * <p>
         * Should return a non-null value
         * @return
         */
        public Bitmap getGameBackground();

    }

}