package com.oursaviorgames.android.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.Date;

import com.oursaviorgames.android.BuildConfig;
import com.oursaviorgames.android.data.metastore.BooleanMetaKey;
import com.oursaviorgames.android.data.metastore.DateMetaKey;
import com.oursaviorgames.android.data.metastore.MetaKey;
import com.oursaviorgames.android.util.DateUtils;

import static com.oursaviorgames.android.data.DataContractUtils.dirMimeType;
import static com.oursaviorgames.android.data.DataContractUtils.itemMimeType;


public final class UserDataContract {

    /**
     * Content authority.
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".UserDataProvider";

    /**
     * Base of all Uri's for this content authority.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Table paths (appended to base content uri for possible Uri's).
     */
    public static final String PATH_PLAY_TOKENS = "play_tokens";
    public static final String PATH_VOTES       = "votes";
    public static final String PATH_EXTRA       = "extra";

    /**
     * Extra meta data associated with the user.
     */
    public static class Extra {

        private static final Uri URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_EXTRA).build();

        /**
         * Whether user profile picture is uploaded or not.
         */
        public static BooleanMetaKey META_PROFILE_PICTURE_UPLOADED = new BooleanMetaKey(URI, "profile_uploaded", false);

    }

    /**
     * Define content of play_tokens table.
     */
    public static class PlayTokensEntry implements BaseColumns, StatusColumn {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLAY_TOKENS).build();

        public static final String CONTENT_TYPE =
                dirMimeType(CONTENT_AUTHORITY, PATH_PLAY_TOKENS);

        public static final String CONTENT_ITEM_TYPE =
                itemMimeType(CONTENT_AUTHORITY, PATH_PLAY_TOKENS);

        public static final String TABLE_NAME = "play_tokens";

        /**
         * DateTime when this token was generated.
         * <P>TYPE: TEXT (RFC-3339)</P>
         */
        public static final String COLUMN_TIME_STAMP = "timestamp";

        /**
         * Unique id of the game (same as on server).
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_GAME_ID = "game_id";

        /**
         * Amount of play time in seconds.
         * <P>Type: INTEGER</P>
         */
        public static final String COLUMN_PLAY_DURATION = "play_duration";

        public static Uri buildPlayTokenUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * Define content of votes table.
     */
    @Deprecated
    public static class VotesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_VOTES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_VOTES;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_VOTES;

        public static final String TABLE_NAME = "votes";

        /**
         * Unique id of the game (same as on the server).
         * <P>Type: TEXT (String)</P>
         */
        public static final String COLUMN_GAME_ID = "game_id";

        /**
         * Type of vote.
         * TODO: fix the line below with the enum thingy.
         * <P>Type: TEXT (Mobilebackend VOTE enum)</P>
         */
        public static final String COLUMN_VOTE_TYPE = "vote_type";

        /**
         * Whether this vote has been synced with the server or not.
         * <P>Type: INTEGER (boolean)</P>
         */
        public static final String COLUMN_SYNCED = "synced";

        /**
         * Date and time when this vote was casted or changed.
         * <P>Type: TEXT (RFC-3339)</P>
         */
        public static final String COLUMN_TIME_STAMP = "timestamp";

        /**
         * Meta data for when votes where last synced.
         * <P>Default: {@link DateUtils#REALLY_OLD_DATE}.</P>
         */
        public static final MetaKey<Date> META_LAST_SYNCED =
                new DateMetaKey(CONTENT_URI, "last_synced", DateUtils.REALLY_OLD_DATE);

        /**
         * Meta data for when votes where last changed.
         * <P>Default: {@link DateUtils#REALLY_OLD_DATE}</P>
         */
        public static final MetaKey<Date> META_LAST_CHANGED =
                new DateMetaKey(CONTENT_URI, "last_changed", DateUtils.REALLY_OLD_DATE);


        public static Uri buildVoteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}
