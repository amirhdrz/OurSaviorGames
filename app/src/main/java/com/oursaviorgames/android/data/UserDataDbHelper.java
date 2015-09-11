package com.oursaviorgames.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.oursaviorgames.android.data.UserDataContract.PlayTokensEntry;

/**
 * Manages database for user generated data.
 * All of this data is also synced with the server.
 */
public class UserDataDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    // Set to public for testing only.
    public static final String DATABASE_NAME = "userdata.db";

    public UserDataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /* SQL statements for database creation. */
        final String SQL_CREATE_PLAY_TOKENS_TABLE = "CREATE TABLE " + PlayTokensEntry.TABLE_NAME + " (" +
                PlayTokensEntry._ID + " INTEGER PRIMARY KEY, " +
                PlayTokensEntry.COLUMN_TIME_STAMP + " TEXT NOT NULL, " +
                PlayTokensEntry.COLUMN_GAME_ID + " TEXT NOT NULL, " +
                PlayTokensEntry.COLUMN_PLAY_DURATION + " INTEGER NOT NULL, " +
                PlayTokensEntry._STATUS + " INTEGER DEFAULT " + StatusColumn.NONE + ");";

        final String SQL_CREATE_PLAY_TOKENS_INDEX =
                SqlUtils.createIndex(PlayTokensEntry.TABLE_NAME, PlayTokensEntry._STATUS);

        /* Executes SQL statements */
        db.execSQL(SQL_CREATE_PLAY_TOKENS_TABLE);

        /* Builds Indices on tables */
        db.execSQL(SQL_CREATE_PLAY_TOKENS_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}
