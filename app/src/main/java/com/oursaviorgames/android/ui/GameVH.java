package com.oursaviorgames.android.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.oursaviorgames.android.BuildConfig;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.GameProviderHelper;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.data.model.GameInfoModel;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.widget.TagView;
import com.oursaviorgames.android.ui.widget.VideoView;
import com.oursaviorgames.android.util.TypefaceUtils;
import com.oursaviorgames.android.util.UiUtils;
import com.oursaviorgames.android.util.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Game holder.
 */
public class GameVH implements View.OnClickListener {

    private static final String TAG = makeLogTag(GameVH.class);

    private static final float CARD_VIEW_CORNER_RADIUS_DIPS = 2.f;

    @InjectView(R.id.cardView)
    CardView cardView;

    @InjectView(R.id.videoView)
    VideoView videoView;

    @InjectView(R.id.tagView)
    TagView tagView;

    @InjectView(R.id.gameTitle)
    TextView gameTitle;

    @InjectView(R.id.developerTitle)
    TextView developerTitle;

    @InjectView(R.id.playCount)
    TextView playCount;

    @InjectView(R.id.commentButton)
    View commentButton;

    @InjectView(R.id.shareButton)
    View shareButton;

    @InjectView(R.id.favoriteButton)
    View favoriteButton;

    private final Context context;
    private GameInfoModel model;

    public GameVH(View view) {
        ButterKnife.inject(this, view);
        context = view.getContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cardView.setRadius(UiUtils.convertDipToPx(context, CARD_VIEW_CORNER_RADIUS_DIPS));
        } else {
            cardView.setRadius(0.f);
        }

        videoView.setOnClickListener(this);
        gameTitle.setOnClickListener(this);
        commentButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        favoriteButton.setOnClickListener(this);
    }

    public void bindModel(final GameInfoModel model) {
        this.model = model;
        gameTitle.setText(model.getGameTitle());
        developerTitle.setText(model.getDeveloperName());
        playCount.setText(getCounterString(model.getPlayCount()));
        favoriteButton.setSelected(model.doesLike());
        if (model.hasPlayed()) {
            TypefaceUtils.applyTypeface(gameTitle, TypefaceUtils.ROBOTO_LIGHT);
        } else {
            TypefaceUtils.applyTypeface(gameTitle, TypefaceUtils.ROBOTO_MEDIUM);
        }
    }

    @Override
    public void onClick(View v) {
        checkNotNull(model, "Null model. Set by calling bindModel");

        if (videoView == v || gameTitle == v) {
            GameLoaderActivity.startActionLoadGame(context, model);
        } else if (commentButton == v) {
            CommentActivity.startActivity(context, model.getGameId());
        } else if (shareButton == v) {
            Utils.startActivityShareGame(context, model);
        } else if (favoriteButton == v) {
            final boolean doesLike = model.doesLike();
            if (v.isSelected() != doesLike) {
                // If the button's selected state is not reflected
                // by the model yet, don't do anything.
                return;
            }
            // For a fast performance impression, we set the visual state of the button
            // before we call the listener of the event change.
            v.setSelected(!doesLike);
            if (doesLike) {
                // remove game from SavedGameEntry
                GameProviderHelper.deleteSavedGame(videoView.getContext(), model.getGameId())
                        .subscribe(new AndroidSubscriber<Void>() {
                            @Override
                            public void onNext(Void aVoid) {
                                // done.
                            }
                        });
            } else {
                GameProviderHelper.insertSavedGame(videoView.getContext(), model)
                        .subscribe(new AndroidSubscriber<Uri>() {
                            @Override
                            public void onNext(Uri uri) {
                                //done.
                            }
                        });
            }

        }
    }

    private static String getCounterString(int playCount) {
        return String.format("%04d", playCount);
    }
}
