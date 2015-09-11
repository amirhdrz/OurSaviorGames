package com.oursaviorgames.android.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.oursaviorgames.android.BuildConfig;
import com.oursaviorgames.android.data.metastore.DateMetaKey;
import com.oursaviorgames.android.data.metastore.StringMetaKey;
import com.oursaviorgames.android.data.sql.Alias;
import com.oursaviorgames.android.util.DateUtils;

import static com.oursaviorgames.android.data.DataContractUtils.dirMimeType;
import static com.oursaviorgames.android.data.DataContractUtils.itemMimeType;

/**
 * This class contains convenience methods to query content provider.
 * <p>
 * The paths that end with _LIST represent tables that result from a join query,
 * they do not denote an actual database table.
 */
public final class GameContract {

    /**
     * Name of the content provider
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".GameProvider";

    /**
     * Base of all URI's
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Table paths (appended to base content uri for possible URI's).
     * Paths ending with _LIST represent paths to database queries that return
     * a new type of table (usually result of a database join operation).
     */
    public static final String PATH_NEW_GAMES         = "new_games";
    public static final String PATH_HOT_GAMES         = "hot_games";
    public static final String PATH_SAVED_GAMES       = "saved_games";
    public static final String PATH_PLAY_HISTORY      = "play_history";
    public static final String PATH_EXTRA_GAMES       = "extra_games";
    public static final String PATH_NEW_GAME_LIST     = "new_game_list";
    public static final String PATH_HOT_GAME_LIST     = "hot_game_list";
    public static final String PATH_SAVED_GAME_LIST   = "saved_game_list";
    public static final String PATH_PLAY_HISTORY_LIST = "play_history_list";
    public static final String PATH_COMMENTS          = "comments";

    /**
     * Defines content of comments table.
     */
    public static class CommentsEntry implements BaseColumns, StatusColumn {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENTS).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_COMMENTS);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_COMMENTS);

        public static final String TABLE_NAME = "comments";

        /**
         * Comment Id. Not null.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String COLUMN_COMMENT_ID = "comment_id";

        /**
         * Unique id of the game (same as on server).
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_GAME_ID = "game_id";

        /**
         * Comment timestamp.
         * <P>Type: TEXT (RFC-3339 encoding)</P>
         */
        public static final String COLUMN_TIMESTAMP = "date_published";

        /**
         * Websafe key of the comment's author.
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_AUTHOR_ID = "author_id";

        /**
         * Comment's author's username.
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_AUTHOR_USERNAME = "author_username";

        /**
         * Url of the author profile image.
         * Can be null.
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_AUTHOR_PROFILE_IMAGE = "author_profile";

        /**
         * Comment's message.
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_MESSAGE = "message";

        /**
         * Whether this comment has been flagged as being inappropriate.
         * <P>Type: INTEGER (boolean) </P>
         */
        public static final String COLUMN_FLAGGED = "flagged";

    }

    /**
     * Defines content of extra_games table.
     */
    public static class ExtraGamesEntry implements BaseGameColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXTRA_GAMES).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_EXTRA_GAMES);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_EXTRA_GAMES);

        public static final String TABLE_NAME = "extra_games";

        /**
         * Unique row key.
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_KEY = "key";

        /**
         * SavedGameEntry _ID column alias.
         */
        public static final String COLUMN_SAVED_TABLE_ID_ALIAS =
                SavedGameEntry.TABLE_NAME + "_" + SavedGameEntry._ID;

    }

    /**
     * Defines content of play_history table.
     */
    public static class PlayHistoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAY_HISTORY).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_PLAY_HISTORY);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_PLAY_HISTORY);

        public static final String TABLE_NAME = "play_history";

        /**
         * Unique id of the game (same as on server).
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_GAME_ID = "game_id";

        /**
         * Number of times this game has been played.
         * <P>TYPE: INTEGER (int)</P>
         */
        public static final String COLUMN_USER_PLAY_COUNT = "user_play_count";

        /**
         * Total play time duration in seconds.
         * <P>TYPE: INTEGER (int)</P>
         */
        public static final String COLUMN_PLAY_DURATION = "play_duration";

        /**
         * Last time this game was played.
         * <P>TYPE: TEXT (RFC-3339)</P>
         */
        public static final String COLUMN_LAST_PLAYED = "last_played";

        public static Uri buildPlayHistoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Defines content of saved_games table.
     */
    public static class SavedGameEntry implements BaseGameColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SAVED_GAMES).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_SAVED_GAMES);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_SAVED_GAMES);

        public static final String TABLE_NAME = "saved_games";

        /**
         * DateTime when this game was added to database.
         * <P>Type: TEXT (RFC-3339)</P>
         */
        public static final String COLUMN_DATE_SAVED = "date_saved";

        public static Uri buildSavedGameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /**
     * Defines content of new_games table.
     */
    public static class NewGameEntry implements BaseGameColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEW_GAMES).build();

        public static final String CONTENT_TYPE      =
                dirMimeType(CONTENT_AUTHORITY, PATH_HOT_GAMES);
        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_HOT_GAMES);

        public static final String TABLE_NAME = "new_games";

        public static final DateMetaKey META_LAST_UPDATED =
                new DateMetaKey(CONTENT_URI, "last_updated", DateUtils.REALLY_OLD_DATE);

        /**
         * Next page token meta
         * <P>Default: null</P>
         */
        public static final StringMetaKey META_NEXT_PAGE_TOKEN =
                new StringMetaKey(CONTENT_URI, "nextPageToken", null);

        public static Uri buildNewGameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Defines content of hot_games table.
     */
    public static class HotGameEntry implements BaseGameColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOT_GAMES).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_NEW_GAMES);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_NEW_GAMES);

        public static final String TABLE_NAME = "hot_games";

        /**
         * Next page token meta.
         * <P>Default: null</P>
         */
        public static final StringMetaKey META_NEXT_PAGE_TOKEN =
                new StringMetaKey(CONTENT_URI, "nextPageToken", null);

        public static Uri buildHotGameUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /*
     * -----------------------------------------------------------
     * *ListEntries represent result tables from a database query.
     * -----------------------------------------------------------
     */

    /**
     * Represents columns of left outer join between NewGameEntry and SavedGameEntry
     * and HotGameEntry and SavedGameEntry.
     */
    public static interface GameListColumns extends BaseGameColumns {

        /**
         * SavedGameEntry _ID column alias.
         */
        public static final String COLUMN_SAVED_TABLE_ID_ALIAS =
                SavedGameEntry.TABLE_NAME + "_" + SavedGameEntry._ID;

        /**
         * PlayHistoryEntry _ID column alias.
         */
        public static final String COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS =
                PlayHistoryEntry.TABLE_NAME + "_" + PlayHistoryEntry._ID;

    }

    /**
     * Entry representing left outer join between {@link NewGameEntry} and {@link SavedGameEntry}
     * and {@link PlayHistoryEntry}.
     */
    public static class NewGameListEntry implements GameListColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEW_GAME_LIST).build();

    }

    /**
     * Entry represents left outer join between {@link HotGameEntry} and {@link SavedGameEntry}
     * and {@link PlayHistoryEntry}.
     */
    public static class HotGameListEntry implements GameListColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HOT_GAME_LIST).build();

    }

    /**
     * Entry represents left outer join between {@link SavedGameEntry} and {@link PlayHistoryEntry}.
     */
    public static class SavedGameListEntry implements BaseGameColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SAVED_GAME_LIST).build();

        /**
         * PlayHistoryEntry _ID column alias.
         */
        public static final Alias COLUMN_PLAY_HISTORY_ID =
                new Alias(PlayHistoryEntry.TABLE_NAME, PlayHistoryEntry._ID);

        /**
         * Last time this game was played.
         * <P>TYPE: TEXT (RFC-3339)</P>
         */
        public static final Alias COLUMN_LAST_PLAYED =
                new Alias(PlayHistoryEntry.TABLE_NAME, PlayHistoryEntry.COLUMN_LAST_PLAYED);

        /**
         * DateTime when this game was added to database.
         * <P>Type: TEXT (RFC-3339)</P>
         */
        public static final Alias COLUMN_DATE_SAVED =
                new Alias(SavedGameEntry.TABLE_NAME, SavedGameEntry.COLUMN_DATE_SAVED);

    }

    public static class PlayHistoryListEntry extends PlayHistoryEntry {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAY_HISTORY_LIST).build();

        /**
         * SavedGameEntry _ID column alias.
         */
        public static final String COLUMN_SAVED_TABLE_ID_ALIAS =
                SavedGameEntry.TABLE_NAME + "_" + SavedGameEntry._ID;

    }

}
