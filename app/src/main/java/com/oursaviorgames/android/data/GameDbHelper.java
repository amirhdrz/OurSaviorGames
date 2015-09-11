package com.oursaviorgames.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.data.GameContract.ExtraGamesEntry;
import com.oursaviorgames.android.data.GameContract.HotGameEntry;
import com.oursaviorgames.android.data.GameContract.NewGameEntry;
import com.oursaviorgames.android.data.GameContract.PlayHistoryEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameEntry;

/**
 * Manages local database for games listings and saved games.
 * This class makes it easy for ContentProvider implementations to defer opening
 * and upgrading the database until first use, to avoid blocking application startup with
 * long-running database upgrades.
 */
public class GameDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;

    // Set to public for testing only
    public static final String DATABASE_NAME = "game.db";


    public GameDbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //TODO: create an index on the much queried 'gameId' column.
    @Override
    public void onCreate(SQLiteDatabase db) {

        /* SQL statement for table creation */
        final String SQL_CREATE_HOT_GAMES_TABLE = "CREATE TABLE " + HotGameEntry.TABLE_NAME + " (" +
                HotGameEntry._ID + " INTEGER PRIMARY KEY, " +
                buildSQLBaseGameColumns(true) + " );";

        final String SQL_CREATE_NEW_GAMES_TABLE = "CREATE TABLE " + NewGameEntry.TABLE_NAME + " (" +
                NewGameEntry._ID + " INTEGER PRIMARY KEY, " +
                buildSQLBaseGameColumns(true) + " );";

        final String SQL_CREATE_SAVED_GAMES_TABLE = "CREATE TABLE " + SavedGameEntry.TABLE_NAME + " (" +
                SavedGameEntry._ID + " INTEGER PRIMARY KEY, " +
                SavedGameEntry.COLUMN_DATE_SAVED + " TEXT NOT NULL, "  +
                buildSQLBaseGameColumns(true) + " );";

        final String SQL_CREATE_PLAY_HISTORY_TABLE = "CREATE TABLE " + PlayHistoryEntry.TABLE_NAME + " (" +
                PlayHistoryEntry._ID + " INTEGER PRIMARY KEY, " +
                PlayHistoryEntry.COLUMN_USER_PLAY_COUNT + " INTEGER DEFAULT 0, " +
                PlayHistoryEntry.COLUMN_PLAY_DURATION + " INTEGER DEFAULT 0, " +
                PlayHistoryEntry.COLUMN_LAST_PLAYED + " TEXT NOT NULL, " +
                PlayHistoryEntry.COLUMN_GAME_ID + " TEXT UNIQUE NOT NULL);";

        final String SQL_CREATE_EXTRA_GAMES_TABLE = "CREATE TABLE " + ExtraGamesEntry.TABLE_NAME + " (" +
                ExtraGamesEntry._ID + " INTEGER PRIMARY KEY, " +
                ExtraGamesEntry.COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
                // different game id's in this database are not required to be unique.
                buildSQLBaseGameColumns(false) + " );";

        final String SQL_CREATE_COMMENTS_TABLE = "CREATE TABLE " + CommentsEntry.TABLE_NAME + " (" +
                CommentsEntry._ID + " INTEGER PRIMARY KEY, " +
                CommentsEntry.COLUMN_COMMENT_ID + " INTEGER UNIQUE NOT NULL, " +
                CommentsEntry.COLUMN_GAME_ID + " TEXT NOT NULL, " +
                CommentsEntry.COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
                CommentsEntry.COLUMN_AUTHOR_ID + " TEXT NOT NULL, " +
                CommentsEntry.COLUMN_AUTHOR_USERNAME + " TEXT NOT NULL, " +
                CommentsEntry.COLUMN_AUTHOR_PROFILE_IMAGE + " TEXT, " +
                CommentsEntry.COLUMN_MESSAGE + " TEXT NOT NULL, " +
                CommentsEntry.COLUMN_FLAGGED + " INTEGER DEFAULT 0, " +
                CommentsEntry._STATUS + " INTEGER DEFAULT " + StatusColumn.NONE + ");";


        /* Executes SQL statements */
        db.execSQL(SQL_CREATE_HOT_GAMES_TABLE);
        db.execSQL(SQL_CREATE_NEW_GAMES_TABLE);
        db.execSQL(SQL_CREATE_SAVED_GAMES_TABLE);
        db.execSQL(SQL_CREATE_PLAY_HISTORY_TABLE);
        db.execSQL(SQL_CREATE_EXTRA_GAMES_TABLE);
        db.execSQL(SQL_CREATE_COMMENTS_TABLE);

        /* Executing index building statements */
        db.execSQL(SqlUtils.createUniqueIndex(SavedGameEntry.TABLE_NAME, SavedGameEntry.COLUMN_GAME_ID));
        db.execSQL(SqlUtils.createUniqueIndex(PlayHistoryEntry.TABLE_NAME, PlayHistoryEntry.COLUMN_GAME_ID));
        db.execSQL(SqlUtils.createIndex(CommentsEntry.TABLE_NAME, CommentsEntry.COLUMN_GAME_ID));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply discard the data.

        db.execSQL("DROP TABLE IF EXISTS " + HotGameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + NewGameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ExtraGamesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommentsEntry.TABLE_NAME);
        //db.execSQL("DROP TABLE IF EXISTS " + SavedGameEntry.TABLE_NAME); DO NOT DROP SAVED GAMES!!!

        //TODO:

        onCreate(db);
    }

    /*
        Helper methods.
     */

    /** Convenience method for building base game columns */
    private static String buildSQLBaseGameColumns(boolean uniqueGameId) {
        //TODO: this is a mess.
        String[] statement = new String[11];
        if (uniqueGameId) {
            statement[0] = BaseGameColumns.COLUMN_GAME_ID + " TEXT UNIQUE NOT NULL";
        } else {
            statement[0] = BaseGameColumns.COLUMN_GAME_ID + " TEXT NOT NULL";
        }
        statement[1] = BaseGameColumns.COLUMN_GAME_TITLE + " TEXT NOT NULL";
        statement[2] = BaseGameColumns.COLUMN_GAME_DESCRIPTION + " TEXT";
        statement[3] = BaseGameColumns.COLUMN_DEVELOPER_ID + " TEXT NOT NULL";
        statement[4] = BaseGameColumns.COLUMN_DEVELOPER_NAME + " TEXT NOT NULL";
        statement[5] = BaseGameColumns.COLUMN_GAME_VERSION + " INTEGER NOT NULL";
        statement[6] = BaseGameColumns.COLUMN_DATE_PUBLISHED + " TEXT NOT NULL";
        statement[7] = BaseGameColumns.COLUMN_HOT_SCORE + " INTEGER NOT NULL";
        statement[8] = BaseGameColumns.COLUMN_PLAY_COUNT + " INTEGER NOT NULL";
        statement[9] = BaseGameColumns.COLUMN_OFFLINE + " INTEGER NOT NULL";
        statement[10] = BaseGameColumns.COLUMN_ORIGIN_URL + " TEXT";

        return TextUtils.join(", ", statement);
    }

}
