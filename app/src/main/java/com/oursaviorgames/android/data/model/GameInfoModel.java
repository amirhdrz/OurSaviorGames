package com.oursaviorgames.android.data.model;

import android.content.ContentValues;
import android.net.Uri;
import com.oursaviorgames.android.data.GameContract;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Immutable game info data model.
 */
public class GameInfoModel extends BaseGameModel {

    private static final String TAG = makeLogTag(GameInfoModel.class);

    private final boolean hasPlayed;
    private final boolean doesLike;

    public GameInfoModel(ContentValues baseGameContentValues, boolean hasPlayed, boolean doesLike) {
        super(baseGameContentValues);
        this.hasPlayed = hasPlayed;
        this.doesLike = doesLike;
    }

    public boolean hasPlayed() {
        return hasPlayed;
    }

    public boolean doesLike() {
        return doesLike;
    }

    public static GameInfoModel createFromContentValues(Uri contentUri, ContentValues v) {
        boolean doesLike, hasPlayed;

        if (GameContract.NewGameListEntry.CONTENT_URI.equals(contentUri)
                || GameContract.HotGameListEntry.CONTENT_URI.equals(contentUri)) {
            // If contentUri is from one of GameListColumns entries.
            doesLike = v.getAsString(GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS) != null;
            hasPlayed = v.getAsString(GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS) != null;
        } else if (GameContract.SavedGameListEntry.CONTENT_URI.equals(contentUri)) {
            // If contentUri is from SavedGameListEntry.
            doesLike = true; //implicitly true.
            hasPlayed = v.getAsString(GameContract.SavedGameListEntry.COLUMN_PLAY_HISTORY_ID.alias) != null;
        } else if (GameContract.PlayHistoryListEntry.CONTENT_URI.equals(contentUri)) {
            // If contentUri is from PlayHistoryListEntry.
            doesLike = v.getAsString(GameContract.PlayHistoryListEntry.COLUMN_SAVED_TABLE_ID_ALIAS) != null;
            hasPlayed = true; //implicitly true.
        } else if (GameContract.ExtraGamesEntry.CONTENT_URI.equals(contentUri)) {
            doesLike = v.getAsString(GameContract.ExtraGamesEntry.COLUMN_SAVED_TABLE_ID_ALIAS) != null;
            hasPlayed = false;
        } else {
            throw new IllegalArgumentException("Unknown Uri: " + contentUri.toString());
        }

        return new GameInfoModel(v, hasPlayed, doesLike);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof GameInfoModel) {
            GameInfoModel model = (GameInfoModel) o;
            if (this.hasPlayed != model.hasPlayed) {
                return false;
            }
            if (this.doesLike != model.doesLike) {
                return false;
            }
            return super.equals(o);
        }
        return false;
    }
}
