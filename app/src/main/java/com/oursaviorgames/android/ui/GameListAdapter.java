package com.oursaviorgames.android.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.model.GameInfoModel;
import com.oursaviorgames.android.moonplayer.MoonPlayer;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class GameListAdapter extends CursorAdapter implements AbsListView.OnScrollListener {

    private static final String TAG = makeLogTag(GameListAdapter.class);

    private final Context    mContext;
    private final MoonPlayer mMoonPlayer;

    private Uri        mContentUri;

    // We don't want to request the same id more than once.
    @Deprecated
    int mLastRequestedId = -1;

    /**
     * User {@link #swapCursor(android.database.Cursor)} to set this
     * adapter's Cursor.
     * @param context
     * @param moonPlayer
     */
    public GameListAdapter(Context context, MoonPlayer moonPlayer) {
        super(context, null, 0);
        mContext = context;
        mMoonPlayer = moonPlayer;
    }

    @Deprecated
    @Override
    public final Cursor swapCursor(Cursor newCursor) {
        throw new UnsupportedOperationException("Use #swapCursor(Cursor, Uri)");
    }

    public Cursor swapCursor(Cursor newCursor, Uri contentUri) {
        LOGD(TAG, "swapCursor:: contentUri: " + contentUri.toString());
        mLastRequestedId = -1;
        mContentUri = contentUri;
        return super.swapCursor(newCursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_game, parent, false);
        GameVH holder = new GameVH(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        LOGD(TAG, "bindView:: position: " + cursor.getPosition());
        // Retrieve data.
        final ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);

        final GameInfoModel model = GameInfoModel.createFromContentValues(mContentUri, values);

        // Bind data.
        GameVH holder = (GameVH) view.getTag();
        holder.bindModel(model);

        // register VideoView with MoonPlayer.
        mMoonPlayer.registerSurface(cursor.getPosition(), model.getGameId(), holder.videoView);
    }

    /*
        OnScroll Listener
     */

    /**
     * Determines the position within listView to play.
     * @return -1 if no listView is empty.
     */
    private int getPosToPlay(AbsListView listView) {
        View topView = listView.getChildAt(0);
        if (topView != null) {
            Rect topVisibleRect = new Rect(0, 0, topView.getWidth(), topView.getHeight());
            listView.getChildVisibleRect(topView, topVisibleRect, null);

            float visiblePortion = (float) topVisibleRect.height() / (float) topView.getHeight();

            int firstVisiblePosition = listView.getFirstVisiblePosition();

            return (visiblePortion >= 0.7f) ? firstVisiblePosition : firstVisiblePosition + 1;
        } else {
            return -1;
        }
    }
    boolean isFlung = false;
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        LOGD(TAG, "onScrollStateChanged:: scrollState " + scrollState);
        isFlung = (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING);
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            int posToPlay = getPosToPlay(view);
            if (posToPlay != -1 && mLastRequestedId != posToPlay) {
                LOGD(TAG, "onScrollStateChanged:: preparing player: id:" + posToPlay);
                mMoonPlayer.preparePlayer(posToPlay);
            }
        }
    }

    @Override
    public void onScroll(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!isFlung) {
            int posToPlay = getPosToPlay(listView);
            if (posToPlay != -1 && posToPlay != mLastRequestedId) {
                LOGD(TAG, "onScroll:: preparing player: id:" + posToPlay);
                mMoonPlayer.preparePlayer(posToPlay);
                mLastRequestedId = posToPlay;
            }
        }
    }

}
