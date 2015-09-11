package com.oursaviorgames.android.backend;

import android.content.ContentValues;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.CommentResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameResponse;

import java.util.List;

import com.oursaviorgames.android.data.BaseGameColumns;
import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.util.DateUtils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Helper class to convert server response object to {@link ContentValues}
 * in ordered be stored to be database.
 */
public class BackendResponseHelper {

    private static final String TAG = makeLogTag(BackendResponseHelper.class);

    /**
     * Puts {@link BaseGameColumns} columns from GameBean into ContentValues.
     */
    public static void gameResponseToContentValues(ContentValues v, GameResponse game) {
        v.put(BaseGameColumns.COLUMN_GAME_ID, game.getGameId());
        v.put(BaseGameColumns.COLUMN_GAME_TITLE, game.getTitle());
        v.put(BaseGameColumns.COLUMN_GAME_DESCRIPTION, game.getShortDescription());
        v.put(BaseGameColumns.COLUMN_DEVELOPER_ID, game.getDeveloperId());
        v.put(BaseGameColumns.COLUMN_DEVELOPER_NAME, game.getDeveloperName());
        v.put(BaseGameColumns.COLUMN_GAME_VERSION, game.getVersion());
        v.put(BaseGameColumns.COLUMN_DATE_PUBLISHED,
                DateUtils.getTimestampString(game.getDatePublished()));
        v.put(BaseGameColumns.COLUMN_HOT_SCORE, game.getHotScore());
        v.put(BaseGameColumns.COLUMN_PLAY_COUNT, game.getPlayCount());
        v.put(BaseGameColumns.COLUMN_OFFLINE, game.getIsPlayableOffline());
        v.put(BaseGameColumns.COLUMN_ORIGIN_URL, game.getOriginUrl());
    }

    public static BaseGameModel gameResponseToGameModel(GameResponse game) {
        ContentValues cv = new ContentValues(11);
        gameResponseToContentValues(cv, game);
        return new BaseGameModel(cv);
    }

    /**
     * Populates v with comment fields based on {@link CommentsEntry} columns.
     */
    public static void commentResponseToContentValues(ContentValues v, CommentResponse comment) {
        v.put(CommentsEntry.COLUMN_COMMENT_ID, comment.getCommentId());
        v.put(CommentsEntry.COLUMN_GAME_ID, comment.getGameId());
        v.put(CommentsEntry.COLUMN_TIMESTAMP, DateUtils.getTimestampString(comment.getTimestamp()));
        v.put(CommentsEntry.COLUMN_MESSAGE, comment.getMessage());
        v.put(CommentsEntry.COLUMN_AUTHOR_ID, comment.getAuthorId());
        v.put(CommentsEntry.COLUMN_AUTHOR_USERNAME, comment.getAuthorUsername());
        v.put(CommentsEntry.COLUMN_AUTHOR_PROFILE_IMAGE, comment.getAuthorThumbUrl());
        v.put(CommentsEntry.COLUMN_FLAGGED, comment.getFlagged());
    }

    /**
     * Converts {@link GameCollectionResponse} to an array of {@link BaseGameColumns} ContentValues.
     * <p>
     * @return array of content values or null if collection is null or contains null game list.
     */
    public static ContentValues[] gameCollectionResponseToContentValues(GameCollectionResponse collection) {
        if (collection == null) return null;
        List<GameResponse> gameList = collection.getItems();

        if (gameList == null) return null;

        ContentValues[] cvs = new ContentValues[gameList.size()];

        for (int i = 0; i < gameList.size(); i++) {
            GameResponse game = gameList.get(i);
            ContentValues cv = new ContentValues();
            gameResponseToContentValues(cv, game);
            cvs[i] = cv;
        }

        return cvs;
    }

}
