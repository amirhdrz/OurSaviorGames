package com.oursaviorgames.android.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;

import java.util.ArrayList;

import com.oursaviorgames.android.backend.BackendResponseHelper;
import com.oursaviorgames.android.data.GameContract.PlayHistoryEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameEntry;
import com.oursaviorgames.android.data.model.BaseGameModel;
import com.oursaviorgames.android.util.DateUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.oursaviorgames.android.data.SqlUtils.equal;

/**
 * This class contains convenience methods for interacting with {@link GameProvider}.
 * All functions that do not return Observables block until done.
 */
public class GameProviderHelper {

    /**
     * Updates meta-data of {@link SavedGameEntry}.
     * Notifies observers of a {@link java.util.NoSuchElementException} if the returned
     * observable is empty.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     *
     * @param context Applications Context.
     * @return Observable that emits a single null item when update has finished.
     */
    public static Observable<Void> updateSavedGames(final Context context, final GameCollectionResponse response) {
        return Observable.create(new Observable.OnSubscribe<BaseGameModel>() {
            @Override
            public void call(Subscriber<? super BaseGameModel> subscriber) {
                ContentValues[] cvArray = BackendResponseHelper.gameCollectionResponseToContentValues(response);
                if (cvArray != null) {
                    for (ContentValues aCvArray : cvArray) {
                        subscriber.onNext(new BaseGameModel(aCvArray));
                    }
                }
                //TODO: throws sequence contains no elements when FavoritesFragment is empty.
                subscriber.onCompleted();
            }
        })
        .flatMap(new Func1<BaseGameModel, Observable<Void>>() {
            @Override
            public Observable<Void> call(BaseGameModel baseGameModel) {
                return updateSavedGame(context, baseGameModel);
            }
        })
        .last()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates single game in {@link SavedGameEntry} with model.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     *
     * @param context Applications Context.
     */
    public static Observable<Void> updateSavedGame(final Context context, final BaseGameModel model) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                int rowsUpdated = context.getContentResolver().update(
                        SavedGameEntry.CONTENT_URI,
                        model.getContentValues(),
                        equal(SavedGameEntry.COLUMN_GAME_ID, model.getGameId()),
                        null);
                if (rowsUpdated != 1) {
                    subscriber.onError(new IllegalStateException("Row with gameId " +
                    model.getGameId() + " is not unique"));
                } else {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());

    }

    /**
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     *
     * @param context Applications Context.
     * @return Observable emitting list of game ids in {@link SavedGameEntry} table.
     */
    public static Observable<ArrayList<String>> getSavedGamesIds(final Context context) {
        return Observable.create(new Observable.OnSubscribe<ArrayList<String>>() {
            @Override
            public void call(Subscriber<? super ArrayList<String>> subscriber) {
                Cursor cursor = context.getContentResolver().query(
                        SavedGameEntry.CONTENT_URI,
                        new String[] {SavedGameEntry.COLUMN_GAME_ID},
                        null,
                        null,
                        null);

                if (cursor != null) {
                    ArrayList<String> gameIdList = new ArrayList<>(cursor.getCount());
                    while (cursor.moveToNext()) {
                        String gameId = cursor.getString(cursor.getColumnIndex(SavedGameEntry.COLUMN_GAME_ID));
                        gameIdList.add(gameId);
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(gameIdList);
                        subscriber.onCompleted();
                    }
                } else {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new IllegalStateException("Null cursor"));
                    }
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     *
     * @param context Applications Context.
     * @param gameId Id to query for.
     * @return Observable emitting saved game row uri or null if no matching row has been found.
     */
    public static Observable<Uri> getSavedGame(final Context context, final String gameId) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                Cursor cursor = context.getContentResolver().query(
                        GameContract.SavedGameEntry.CONTENT_URI,
                        new String[] {SavedGameEntry._ID, SavedGameEntry.COLUMN_GAME_ID},
                        equal(SavedGameEntry.COLUMN_GAME_ID, gameId),
                        null,
                        null
                );

                Uri row = null;
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        long rowId = cursor.getLong(cursor.getColumnIndex(SavedGameEntry._ID));
                        row = ContentUris.withAppendedId(SavedGameEntry.CONTENT_URI, rowId);
                    }
                    cursor.close();

                    subscriber.onNext(row);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Cursor is null"));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Inserts a new row into {@link SavedGameEntry}.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     * @param context Applications Context.
     * @param model Model containing the game data to insert.
     * @return Observable which emits URL of the newly created row.
     */
    public static Observable<Uri> insertSavedGame(final Context context, final BaseGameModel model) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                ContentValues values = new ContentValues(model.copyContentValues());
                values.put(SavedGameEntry.COLUMN_DATE_SAVED, DateUtils.getCurrentZuluTime());
                Uri row = context.getContentResolver().insert(SavedGameEntry.CONTENT_URI, values);
                if (row != null) {
                    subscriber.onNext(row);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Table: SavedGamesEntry," +
                            " Model: " + model.toString()));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Deletes game with gameId from {@link com.oursaviorgames.android.data.GameContract.SavedGameEntry}.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     * @param gameId game to remove.
     * @return True if successful, False otherwise.
     */
    public static Observable<Void> deleteSavedGame(final Context context, final String gameId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                int rowsDeleted = context.getContentResolver()
                        .delete(GameContract.SavedGameEntry.CONTENT_URI,
                                equal(SavedGameEntry.COLUMN_GAME_ID, gameId),
                                null);

                if (rowsDeleted == 1) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException(
                            "failed to delete row with game id " + gameId));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Emits Uri of the row in {@link PlayHistoryEntry} with gameId.
     * If no row was found, null is emitted.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     *
     *
     * @param context Applications Context.
     * @param gameId game to query.
     * @return Observable which emits Uri of the queried row, or null if query had no match.
     */
    public static Observable<Uri> getPlayHistoryRow(final Context context, final String gameId) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                Cursor cursor = context.getContentResolver().query(
                        PlayHistoryEntry.CONTENT_URI,
                        new String[] {PlayHistoryEntry._ID},
                        equal(PlayHistoryEntry.COLUMN_GAME_ID, gameId),
                        null,
                        null);

                Uri row = null;

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        long rowId = cursor.getLong(cursor.getColumnIndex(PlayHistoryEntry._ID));
                        row = ContentUris.withAppendedId(PlayHistoryEntry.CONTENT_URI, rowId);
                    }
                    cursor.close();

                    subscriber.onNext(row);
                    subscriber.onCompleted();

                } else {
                    subscriber.onError(new IllegalStateException("Null cursor"));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Updates PlayHistoryEntry if a row if gameId already exists, or inserts a new one otherwise.
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded.
     * <p>
     * @param context Applications Context.
     * @param gameId Game to update the PlayHistory for.
     * @param playDuration How long the game has been played for in seconds.
     * @return Observable which emits URL of the newly created row.
     */
    public static Observable<Uri> updatePlayHistory(final Context context, final String gameId,
                                                    final int playDuration) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                // Query database to check if a new row needs to be created or previous one updated.
                Cursor cursor = context.getContentResolver().query(
                        PlayHistoryEntry.CONTENT_URI,
                        null,
                        equal(PlayHistoryEntry.COLUMN_GAME_ID, gameId),
                        null,
                        null);

                long rowId = -1;
                ContentValues previousValues = new ContentValues(5);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        DatabaseUtils.cursorRowToContentValues(cursor, previousValues);
                        rowId = previousValues.getAsLong(PlayHistoryEntry._ID);
                    }
                    cursor.close();
                } else {
                    subscriber.onError(new IllegalStateException("Null cursor"));
                    return;
                }

                // If a row for this game already exists updates it,
                // otherwise creates a new row.
                Uri rowUri;
                if (rowId != -1) {
                    // Creates new updated values.
                    final int updatedPlayDuration = playDuration +
                            previousValues.getAsInteger(PlayHistoryEntry.COLUMN_PLAY_DURATION);
                    final int updatedPlayCount = 1 + previousValues.getAsInteger(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT);
                    final ContentValues values = new ContentValues(3);
                    values.put(PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());
                    values.put(PlayHistoryEntry.COLUMN_PLAY_DURATION, updatedPlayDuration);
                    values.put(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, updatedPlayCount);

                    rowUri = ContentUris.withAppendedId(PlayHistoryEntry.CONTENT_URI, rowId);
                    context.getContentResolver().update(rowUri, values,null, null);
                } else {
                    // Inserting new row into PlayHistoryEntry.
                    ContentValues values = new ContentValues(4);
                    values.put(PlayHistoryEntry.COLUMN_GAME_ID, gameId);
                    values.put(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, 1);
                    values.put(PlayHistoryEntry.COLUMN_PLAY_DURATION, playDuration);
                    values.put(PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());

                    rowUri = context.getContentResolver().insert(PlayHistoryEntry.CONTENT_URI, values);
                }

                if (rowUri != null) {
                    subscriber.onNext(rowUri);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException("Failed to insert/update row"));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

}
