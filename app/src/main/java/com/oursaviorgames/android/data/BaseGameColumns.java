package com.oursaviorgames.android.data;

import android.provider.BaseColumns;

/**
 * Base columns for any table that stores information about games.
 */
public interface BaseGameColumns extends BaseColumns {

    /**
     * Unique id of the game (same as on server).
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_GAME_ID = "game_id";

    /**
     * Title of the game.
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_GAME_TITLE = "title";

    /**
     * Description of the game.
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_GAME_DESCRIPTION = "description";

    /**
     * Id of the developer (same as on the server).
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_DEVELOPER_ID = "developer_id";

    /**
     * Name of the game developer.
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_DEVELOPER_NAME = "developer_name";

    /**
     * Version of the game.
     * <P>Type: INTEGER (int)</P>
     */
    public static final String COLUMN_GAME_VERSION = "game_version";

    /**
     * DateTime game was first created.
     * <P>Type: TEXT (RFC-3339 encoding)</P>
     */
    public static final String COLUMN_DATE_PUBLISHED = "date_published";

    /**
     * Hot score of the game.
     * <P>Type: INTEGER (int)</P>
     */
    public static final String COLUMN_HOT_SCORE = "hot_score";

    /**
     * Number of times the game has been played.
     * <P>Type: INTEGER (int)</P>
     */
    public static final String COLUMN_PLAY_COUNT = "play_count";

    /**
     * Whether this game is playable offline or not.
     * <P>Type: INTEGER (boolean)</P>
     */
    public static final String COLUMN_OFFLINE = "offline";

    /**
     * Game's origin url. Could be null.
     * <P>Type: TEXT (String)</P>
     */
    public static final String COLUMN_ORIGIN_URL = "originUrl";

}

