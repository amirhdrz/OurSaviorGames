package com.oursaviorgames.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.oursaviorgames.android.util.DateUtils;
import rx.Observable;
import rx.Subscriber;

import com.oursaviorgames.android.data.UserDataContract.PlayTokensEntry;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * This class contains convenience methods for interacting with {@link UserDataProvider}.
 * All functions that do not return Observables block until done.
 */
public class UserDataHelper {

    /**
     * Inserts a new row into {@link UserDataContract.PlayTokensEntry}
     * <p>
     * Scheduler: subscribe on {@link rx.schedulers.Schedulers#io()}.
     *            observe on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     * Observable: returned Observable is not bounded by default.
     * <p>
     * @param context Application's context.
     * @param gameId PlayToken's game id.
     * @param playDuration How long was this game played for in seconds.
     * @return Observable which emits URL of the newly created row.
     */
    public static Observable<Uri> insertPlayToken(final Context context, final String gameId,
                                                  final int playDuration) {
        return Observable.create(new Observable.OnSubscribe<Uri>() {
            @Override
            public void call(Subscriber<? super Uri> subscriber) {
                ContentValues values = new ContentValues(3);
                values.put(PlayTokensEntry.COLUMN_GAME_ID, gameId);
                values.put(PlayTokensEntry.COLUMN_PLAY_DURATION, playDuration);
                values.put(PlayTokensEntry.COLUMN_TIME_STAMP, DateUtils.getCurrentZuluTime());

                final Uri row = context.getContentResolver().insert(PlayTokensEntry.CONTENT_URI, values);

                if (row != null) {
                    subscriber.onNext(row);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new IllegalStateException(
                            "Could not insert row into PlayTokensEntry. values: " + values.toString()));
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }



}
