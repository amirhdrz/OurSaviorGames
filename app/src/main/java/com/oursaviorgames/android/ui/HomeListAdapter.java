package com.oursaviorgames.android.ui;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.model.GameInfoModel;
import com.oursaviorgames.android.moonplayer.MoonPlayer;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * ListView Adapter for HomeFragment.
 * <p>
 * Both {@link #setSpotlightGame(GameInfoModel)} and {@link #setResumedGame(GameInfoModel)}
 * have to be called before any items are set.
 *
 */
public class HomeListAdapter extends BaseAdapter implements AbsListView.OnScrollListener{

    private static final String TAG = makeLogTag(HomeListAdapter.class);

    private static final int SPOTLIGHT_GAME_POSITION = 0;
    private static final int RESUME_GAME_POSITION    = 1;

    private final Context                            mContext;
    private final MoonPlayer                         mMoonPlayer;

    private GameInfoModel mSpotlightGame;
    private GameInfoModel mResumeGame;
    private boolean mSpotlightGameSet = false;
    private boolean mResumeGameSet    = false;

    // We don't want to request the same id more than once.
    @Deprecated
    int mLastRequestedId = -1;

    public HomeListAdapter(Context context, MoonPlayer moonPlayer) {
        mContext = context;
        mMoonPlayer = moonPlayer;
    }

    /**
     * Must be set before this adapter returns any views.
     * @param model Spotlight game model.
     */
    public void setSpotlightGame(GameInfoModel model) {
        if (!model.equals(mSpotlightGame)) {
            LOGD(TAG, "setSpotlightGame:: changed");
            mSpotlightGame = model;
            mSpotlightGameSet = true;
            notifyDataSetChanged();
        }
    }

    /**
     * Must be set before this adapter returns any views.
     * <p>
     * {@link #notifyDataSetChanged()} is called automatically after this call.
     *
     * @param model Resumed game model or null if there is no resumed game.
     */
    public void setResumedGame(GameInfoModel model) {
        if (model == null || !model.equals(mResumeGame)) {
            LOGD(TAG, "setResumedGame:: changed");
            mResumeGame = model;
            mResumeGameSet = true;
            notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        // Resume game and spotlight game both have to be set.
        if (mResumeGameSet && mSpotlightGameSet) {
            super.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        mMoonPlayer.stop();
        mSpotlightGameSet = mResumeGameSet = false;
        mResumeGame = mSpotlightGame = null;
        mLastRequestedId = -1;
        super.notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return (mResumeGameSet && mSpotlightGameSet) ? 2 : 0;
    }

    @Override
    public GameInfoModel getItem(int position) {
        return (position == SPOTLIGHT_GAME_POSITION)? mSpotlightGame : mResumeGame;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LOGD(TAG, "getView:: position: " + position);

        if (RESUME_GAME_POSITION == position && mResumeGame == null) {
            // Show empty resume game.
            return LayoutInflater.from(mContext).inflate(R.layout.layout_empty_resume_game, parent, false);

        } else {

            // Resume game can't be recycled if previously layout_empty_resume_game
            // was displayed. We check convertView.getTag() to check for this event.
            if (convertView == null || convertView.getTag() == null) {
                LOGD(TAG, "getView:: new view for position: " + position);
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_game, parent, false);
                GameVH holder = new GameVH(convertView);
                convertView.setTag(holder);

                // Sets games tags.
                if (SPOTLIGHT_GAME_POSITION == position) {
                    holder.tagView.setDrawable(mContext.getResources().getDrawable(R.drawable.ic_star_white_16dp));
                    holder.tagView.setContentBackground(R.color.accentRed);
                } else if (RESUME_GAME_POSITION == position) {
                    holder.tagView.setDrawable(mContext.getResources().getDrawable(R.drawable.ic_continue_white_16dp));
                    holder.tagView.setContentBackground(R.color.accentBlue);
                }
                holder.tagView.setVisibility(View.VISIBLE);
            }

            // Binds model.
            final GameInfoModel model = getItem(position);
            GameVH holder = (GameVH) convertView.getTag();
            holder.bindModel(model);

            mMoonPlayer.registerSurface(position, getItem(position).getGameId(), holder.videoView);

            return convertView;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount > 0) {
            View topView = listView.getChildAt(0);
            Rect visibleRect = new Rect(0, 0, topView.getWidth(), topView.getHeight());
            listView.getChildVisibleRect(topView, visibleRect, null);

            float visiblePortion = (float) visibleRect.height() / (float) topView.getHeight();

            int idToPlay;
            if (mResumeGame != null) {
                idToPlay = (visiblePortion >= 0.8f) ? firstVisibleItem : firstVisibleItem + 1;
            } else {
                idToPlay = firstVisibleItem;
            }
            if (idToPlay != mLastRequestedId) {
                LOGD(TAG, "onScroll:: preparingPlayer(id " + idToPlay + ")");
                mMoonPlayer.preparePlayer(idToPlay);
                mLastRequestedId = idToPlay;
            }
        }
    }

}
