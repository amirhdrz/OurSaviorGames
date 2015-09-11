package com.oursaviorgames.android.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;

import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.data.GameContract.ExtraGamesEntry;
import com.oursaviorgames.android.data.GameContract.HotGameEntry;
import com.oursaviorgames.android.data.GameContract.HotGameListEntry;
import com.oursaviorgames.android.data.GameContract.NewGameEntry;
import com.oursaviorgames.android.data.GameContract.NewGameListEntry;
import com.oursaviorgames.android.data.GameContract.PlayHistoryEntry;
import com.oursaviorgames.android.data.GameContract.PlayHistoryListEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameListEntry;
import com.oursaviorgames.android.util.UriUtils;

import static com.oursaviorgames.android.data.SqlUtils.all;
import static com.oursaviorgames.android.data.SqlUtils.bulkInsertHelper;
import static com.oursaviorgames.android.data.SqlUtils.executeQueryWithId;
import static com.oursaviorgames.android.data.SqlUtils.fieldFromAlias;
import static com.oursaviorgames.android.data.SqlUtils.insertRowOrThrow;
import static com.oursaviorgames.android.data.SqlUtils.sortField;
import static com.oursaviorgames.android.data.SqlUtils.updateSingleRowOrThrow;
import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static org.jooq.impl.DSL.fieldByName;

/**
 * All the overridden methods here except for onCreate() should be thread-safe.
 * Lengthy operations should not be put in onCreate() method.
 * Note that there is no need to close the database in a ContentProvider. It will get closed
 * when the hosting run is cleaned up by the kernel.
 */
public class GameProvider extends ContentProvider{

    private static final String TAG = makeLogTag(GameProvider.class);

    private final static UriMatcher sUriMatcher = buildUriMatcher();
    private final static DSLContext create      = DSL.using(SQLDialect.SQLITE);
    private GameDbHelper mDbHelper;

    /* UriMatcher path matching codes */
    private static final int HOT_GAMES         = 100;
    private static final int HOT_GAMES_ID      = 101;
    private static final int NEW_GAMES         = 200;
    private static final int NEW_GAMES_ID      = 201;
    private static final int SAVED_GAMES       = 300;
    private static final int SAVED_GAMES_ID    = 301;
    private static final int PLAY_HISTORY      = 400;
    private static final int PLAY_HISTORY_ID   = 401;
    private static final int EXTRA_GAMES       = 500;
    private static final int EXTRA_GAMES_ID    = 501;
    private static final int NEW_GAME_LIST     = 600;
    private static final int HOT_GAME_LIST     = 700;
    private static final int SAVED_GAME_LIST   = 800;
    private static final int PLAY_HISTORY_LIST = 900;
    private static final int COMMENTS          = 1000;
    private static final int COMMENTS_ID       = 1001;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GameContract.CONTENT_AUTHORITY;
        //
        matcher.addURI(authority, GameContract.PATH_HOT_GAMES, HOT_GAMES);
        matcher.addURI(authority, GameContract.PATH_NEW_GAMES, NEW_GAMES);
        matcher.addURI(authority, GameContract.PATH_SAVED_GAMES, SAVED_GAMES);
        matcher.addURI(authority, GameContract.PATH_PLAY_HISTORY, PLAY_HISTORY);
        matcher.addURI(authority, GameContract.PATH_EXTRA_GAMES, EXTRA_GAMES);
        matcher.addURI(authority, GameContract.PATH_NEW_GAME_LIST, NEW_GAME_LIST);
        matcher.addURI(authority, GameContract.PATH_HOT_GAME_LIST, HOT_GAME_LIST);
        matcher.addURI(authority, GameContract.PATH_SAVED_GAME_LIST, SAVED_GAME_LIST);
        matcher.addURI(authority, GameContract.PATH_PLAY_HISTORY_LIST, PLAY_HISTORY_LIST);
        matcher.addURI(authority, GameContract.PATH_COMMENTS, COMMENTS);

        matcher.addURI(authority, GameContract.PATH_HOT_GAMES + "/#", HOT_GAMES_ID);
        matcher.addURI(authority, GameContract.PATH_NEW_GAMES + "/#", NEW_GAMES_ID);
        matcher.addURI(authority, GameContract.PATH_SAVED_GAMES + "/#", SAVED_GAMES_ID);
        matcher.addURI(authority, GameContract.PATH_PLAY_HISTORY + "/#", PLAY_HISTORY_ID);
        matcher.addURI(authority, GameContract.PATH_EXTRA_GAMES + "/#", EXTRA_GAMES_ID);
        matcher.addURI(authority, GameContract.PATH_COMMENTS + "/#", COMMENTS_ID);

        return matcher;
    }

    /**
     * Instantiates GameDbHelper.
     * @return true.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new GameDbHelper(getContext());
        return true;
    }

    /**
     * Returned cursor is registered to watch content uri for changes.
     * @param uri The URI to query.
     * @param projection The list of columns to put into the cursor. If null all columns are included.
     * @param selection A selection criteria to apply when filtering rows. If null then all rows are included.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from
     *                      selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @param sortOrder How the rows in the cursor should be sorted. If null then the provider is free to define the sort order.
     * @return a Cursor or null. Cursor watches the uri for notifications.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case HOT_GAMES: {
                retCursor = db.query(
                        HotGameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case NEW_GAMES: {
                retCursor = db.query(
                        NewGameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SAVED_GAMES: {
                retCursor = db.query(
                        SavedGameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SAVED_GAMES_ID: {
                retCursor = executeQueryWithId(
                        db,
                        SavedGameEntry.TABLE_NAME,
                        projection,
                        ContentUris.parseId(uri)
                );
                break;
            }
            case PLAY_HISTORY: {
                retCursor = db.query(
                        PlayHistoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case PLAY_HISTORY_ID: {
                retCursor = executeQueryWithId(
                        db,
                        PlayHistoryEntry.TABLE_NAME,
                        projection,
                        ContentUris.parseId(uri)
                );
                break;
            }
            case EXTRA_GAMES: {
                String SQL_QUERY = SqlUtils.createLeftOuterJoin(
                        ExtraGamesEntry.TABLE_NAME,
                        SavedGameEntry.TABLE_NAME,
                        ExtraGamesEntry.COLUMN_SAVED_TABLE_ID_ALIAS,
                        BaseGameColumns.COLUMN_GAME_ID,
                        null
                );
                if (selection != null) {
                    SQL_QUERY += " WHERE " + selection;
                } else {
                    throw new IllegalArgumentException("selection clause must be part of the query");
                }
                retCursor = db.rawQuery(SQL_QUERY, null);
                break;
            }
            case NEW_GAME_LIST: {
                final String SQL_QUERY = SqlUtils.createLeftOuterJoin(
                        NewGameEntry.TABLE_NAME,
                        SavedGameEntry.TABLE_NAME,
                        PlayHistoryEntry.TABLE_NAME,
                        GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS,
                        GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS,
                        BaseGameColumns.COLUMN_GAME_ID,
                        sortOrder);
                retCursor = db.rawQuery(SQL_QUERY, null);
                break;
            }
            case HOT_GAME_LIST: {
                final String SQL_QUERY = SqlUtils.createLeftOuterJoin(
                        HotGameEntry.TABLE_NAME,
                        SavedGameEntry.TABLE_NAME,
                        PlayHistoryEntry.TABLE_NAME,
                        GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS,
                        GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS,
                        BaseGameColumns.COLUMN_GAME_ID,
                        sortOrder);
                retCursor = db.rawQuery(SQL_QUERY, null);
                break;
            }
            case SAVED_GAME_LIST: {
                final String sql;
                final SelectOnConditionStep partialSql = create.select(
                        all(SavedGameEntry.TABLE_NAME),
                        fieldFromAlias(SavedGameListEntry.COLUMN_DATE_SAVED),
                        fieldFromAlias(SavedGameListEntry.COLUMN_PLAY_HISTORY_ID),
                        fieldFromAlias(SavedGameListEntry.COLUMN_LAST_PLAYED))
                        .from(SavedGameEntry.TABLE_NAME)
                        .leftOuterJoin(PlayHistoryEntry.TABLE_NAME)
                        .on(fieldByName(SavedGameEntry.TABLE_NAME, BaseGameColumns.COLUMN_GAME_ID)
                                .equal(fieldByName(PlayHistoryEntry.TABLE_NAME, BaseGameColumns.COLUMN_GAME_ID)));
                if (sortOrder != null) {
                    sql = partialSql
                            .orderBy(sortField(sortOrder))
                            .getSQL();
                } else {
                    sql = partialSql.getSQL();
                }

                LOGD(TAG, "SAVED_GAME_LIST sql: " + sql);

                retCursor = db.rawQuery(sql, null);
                break;
            }
            case PLAY_HISTORY_LIST: {
                final String SQL_QUERY = SqlUtils.createLeftOuterJoin(
                        PlayHistoryEntry.TABLE_NAME,
                        SavedGameEntry.TABLE_NAME,
                        PlayHistoryListEntry.COLUMN_SAVED_TABLE_ID_ALIAS,
                        BaseGameColumns.COLUMN_GAME_ID,
                        PlayHistoryListEntry.COLUMN_LAST_PLAYED + " DESC"
                )
                        + " LIMIT 1";
                retCursor = db.rawQuery(SQL_QUERY, null);
                break;
            }
            case COMMENTS: {
                retCursor = db.query(CommentsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /**
     * Mime type of uri.
     * @param uri the URI to query.
     * @return a MIME type string, or null if there is no type.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case HOT_GAMES:
                return HotGameEntry.CONTENT_TYPE;
            case HOT_GAMES_ID:
                return HotGameEntry.CONTENT_ITEM_TYPE;
            case NEW_GAMES:
                return NewGameEntry.CONTENT_TYPE;
            case NEW_GAMES_ID:
                return NewGameEntry.CONTENT_ITEM_TYPE;
            case SAVED_GAMES:
                return SavedGameEntry.CONTENT_TYPE;
            case SAVED_GAMES_ID:
                return SavedGameEntry.CONTENT_ITEM_TYPE;
            case PLAY_HISTORY:
                return PlayHistoryEntry.CONTENT_TYPE;
            case PLAY_HISTORY_ID:
                return PlayHistoryEntry.CONTENT_ITEM_TYPE;
            case EXTRA_GAMES:
                return ExtraGamesEntry.CONTENT_TYPE;
            case EXTRA_GAMES_ID:
                return ExtraGamesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * TODO: there is no need to be able to insert rows indivitually into new_games and hot_games tables.
     * Notifies ContentResolver of an update.
     * @param uri The content:// URI of the insertion request. This must not be null.
     * @param values A set of column_name/value pairs to add to the database. This must not be null.
     * @return The URI for the newly inserted item.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case HOT_GAMES: {
                returnUri = insertRowOrThrow(db,
                        HotGameEntry.CONTENT_URI,
                        HotGameEntry.TABLE_NAME,
                        values);
                break;
            }
            case NEW_GAMES: {
                returnUri = insertRowOrThrow(db,
                        NewGameEntry.CONTENT_URI,
                        NewGameEntry.TABLE_NAME,
                        values);
                break;
            }
            case SAVED_GAMES: {
                returnUri = insertRowOrThrow(db,
                        SavedGameEntry.CONTENT_URI,
                        SavedGameEntry.TABLE_NAME,
                        values);
                break;
            }
            case PLAY_HISTORY: {
                returnUri = insertRowOrThrow(db,
                        PlayHistoryEntry.CONTENT_URI,
                        PlayHistoryEntry.TABLE_NAME,
                        values);
                break;
            }
            case EXTRA_GAMES: {
                returnUri = insertRowOrThrow(db,
                        ExtraGamesEntry.CONTENT_URI,
                        ExtraGamesEntry.TABLE_NAME,
                        values);
                break;
            }
            case COMMENTS: {
                returnUri = insertRowOrThrow(db,
                        CommentsEntry.CONTENT_URI,
                        CommentsEntry.TABLE_NAME,
                        values);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notifies observers
        LOGD(TAG, "insert:: notifyObservers(uri: " + uri.toString());
        notifyObservers(match, uri);

        return returnUri;
    }

    /**
     * This method so far does not notify ContentResolver of any changes.
     * A null selection deletes all rows.
     * @param uri The full URI to query, including a row ID (if a specific record is requested).
     * @param selection An optional restriction to apply to rows when deleting.
     * @param selectionArgs
     * @return the number of rows affected if a selection is passed in, 0 otherwise.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch (match) {
            case HOT_GAMES: {
                rowsDeleted = db.delete(HotGameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case NEW_GAMES: {
                rowsDeleted = db.delete(NewGameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case SAVED_GAMES: {
                rowsDeleted = db.delete(SavedGameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PLAY_HISTORY: {
                rowsDeleted = db.delete(PlayHistoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case EXTRA_GAMES: {
                rowsDeleted = db.delete(ExtraGamesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case COMMENTS: {
                rowsDeleted = db.delete(CommentsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Notifies GameList subscribers.
        if (rowsDeleted != 0) {
            LOGD(TAG, "delete:: notifyObservers(uri: " + uri.toString());
            notifyObservers(match, uri);
        }
        return rowsDeleted;
    }

    /**
     * Only implemented for saved_games table and play_history table.
     * Notifies content resolver of any changes.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SAVED_GAMES: {
                rowsUpdated = db.update(
                        SavedGameEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case SAVED_GAMES_ID: {
                rowsUpdated = updateSingleRowOrThrow(db,
                        SavedGameEntry.TABLE_NAME,
                        values,
                        ContentUris.parseId(uri));
                break;
            }
            case PLAY_HISTORY: {
                rowsUpdated = db.update(
                        PlayHistoryEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case PLAY_HISTORY_ID: {
                rowsUpdated = updateSingleRowOrThrow(db,
                        PlayHistoryEntry.TABLE_NAME,
                        values,
                        ContentUris.parseId(uri));
                break;
            }
            case EXTRA_GAMES: {
                rowsUpdated = db.update(
                        ExtraGamesEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            }
            case COMMENTS_ID: {
                rowsUpdated = updateSingleRowOrThrow(db,
                        CommentsEntry.TABLE_NAME,
                        values,
                        ContentUris.parseId(uri));
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Notifies Observers
        if (rowsUpdated != 0) {
            LOGD(TAG, "update:: notifyObservers(uri: " + uri.toString());
            notifyObservers(match, uri);
        }
        return rowsUpdated;
    }

    /**
     * Puts many inserts inside a single database transaction for efficiency.
     * Notifies ContentResolver of changes.
     * @param uri The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database. This must not be null.
     * @return number of rows added to the database.
     */
    @Override
    public int bulkInsert(Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsAdded;

        switch (match) {
            case HOT_GAMES: {
                rowsAdded = bulkInsertHelper(db, HotGameEntry.TABLE_NAME, values);
                break;
            }
            case NEW_GAMES: {
                rowsAdded = bulkInsertHelper(db, NewGameEntry.TABLE_NAME, values);
                break;
            }
            case COMMENTS: {
                rowsAdded = bulkInsertHelper(db, CommentsEntry.TABLE_NAME, values);
                break;
            }
            default:
                // super class still does the insert, just not optimally.
                return super.bulkInsert(uri, values);
        }

        // Notifies observers.
        if (rowsAdded != 0) {
            LOGD(TAG, "bulkInsert:: notifyObservers(uri: " + uri.toString());
            notifyObservers(match, uri);
        }

        return rowsAdded;
    }

    /**
     * Notifies observers of changes in database.
     * @param uriMatcher Uri match code.
     * @param uri affected Uri.
     */
    private void notifyObservers(int uriMatcher, Uri uri) {
        // notify all interested parties except the uri itself.
        if (uriMatcher == SAVED_GAMES || uriMatcher == SAVED_GAMES_ID) {
            LOGD(TAG, "notifyObservers:: notify all SAVED_GAMES observers");
            getContext().getContentResolver().notifyChange(NewGameListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(HotGameListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(ExtraGamesEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(PlayHistoryListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(SavedGameListEntry.CONTENT_URI, null);
        } else if (uriMatcher == PLAY_HISTORY || uriMatcher == PLAY_HISTORY_ID) {
            LOGD(TAG, "notifyObservers:: notify all PLAY_HISTORY observers");
            getContext().getContentResolver().notifyChange(NewGameListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(HotGameListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(SavedGameListEntry.CONTENT_URI, null);
            getContext().getContentResolver().notifyChange(PlayHistoryListEntry.CONTENT_URI, null);
        } else if (uriMatcher == HOT_GAMES || uriMatcher == HOT_GAMES_ID) {
            getContext().getContentResolver().notifyChange(HotGameListEntry.CONTENT_URI, null);
        } else if (uriMatcher == NEW_GAMES || uriMatcher == NEW_GAMES_ID) {
            getContext().getContentResolver().notifyChange(NewGameListEntry.CONTENT_URI, null);
        }
        // notify the uri without the appended id.
        Uri uriToNotify = UriUtils.removeAppendedId(uri);
        LOGD(TAG, "notifyObservers:: uri: " + uriToNotify.toString());
        getContext().getContentResolver().notifyChange(uriToNotify, null);
    }

}
