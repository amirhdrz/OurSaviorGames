package com.oursaviorgames.android.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static com.oursaviorgames.android.data.SqlUtils.updateSingleRowOrThrow;

public class UserDataProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private UserDataDbHelper mDbHelper;

    /* UriMatcher path matching codes */
    private static final int PLAY_TOKENS    = 100;
    private static final int PLAY_TOKENS_ID = 101;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = UserDataContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, UserDataContract.PATH_PLAY_TOKENS, PLAY_TOKENS);
        matcher.addURI(authority, UserDataContract.PATH_PLAY_TOKENS + "/#", PLAY_TOKENS_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new UserDataDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch(match) {
            case PLAY_TOKENS: {
                retCursor = db.query(
                        UserDataContract.PlayTokensEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case PLAY_TOKENS:
                return UserDataContract.PlayTokensEntry.CONTENT_TYPE;
            case PLAY_TOKENS_ID:
                return UserDataContract.PlayTokensEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch(match) {
            case PLAY_TOKENS: {
                long _id = db.insert(UserDataContract.PlayTokensEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = UserDataContract.PlayTokensEntry.buildPlayTokenUri(_id);
                else
                    throw new SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify content resolver of newly added data.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch(match) {
            case PLAY_TOKENS: {
                rowsDeleted = db.delete(UserDataContract.PlayTokensEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case PLAY_TOKENS: {
                rowsUpdated = db.update(
                        UserDataContract.PlayTokensEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case PLAY_TOKENS_ID: {
                rowsUpdated = updateSingleRowOrThrow(db,
                        UserDataContract.PlayTokensEntry.TABLE_NAME,
                        values,
                        ContentUris.parseId(uri));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        return super.bulkInsert(uri, values);
    }
}
