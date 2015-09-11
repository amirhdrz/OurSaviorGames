package com.oursaviorgames.android.backend.processor;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.PlayTokenForm;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.PlayTokenListWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.data.StatusColumn;
import com.oursaviorgames.android.data.UserDataContract.PlayTokensEntry;
import com.oursaviorgames.android.util.DateUtils;

import static com.oursaviorgames.android.data.SqlUtils.equal;
import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Processors for /playtokens endpoint.
 */
public class PlayTokenProcessors {

    public PlayTokenProcessors() {
        // required no-arg constructor.
    }

    /**
     * Uploads play tokens.
     */
    public static class Send extends EndpointProcessor<Void> {
        private static final String TAG = makeLogTag(Send.class);

        private int rowsUploaded;

        public Send(Bundle reqParams) {
            super(reqParams);
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            LOGD(TAG, "preparing to upload play tokens");
            final List<PlayTokenForm> playTokenFormList;

            // Selects all games with NONE status.
            Cursor cursor = context.getContentResolver().query(
                    PlayTokensEntry.CONTENT_URI,
                    null,
                    equal(PlayTokensEntry._STATUS, StatusColumn.NONE),
                    null,
                    null);

            if (cursor != null) {
                playTokenFormList = new ArrayList<>(cursor.getCount());
                while(cursor.moveToNext()) {
                    ContentValues cv = new ContentValues(5);
                    DatabaseUtils.cursorRowToContentValues(cursor, cv);
                    PlayTokenForm form = new PlayTokenForm();
                    form.setGameId(cv.getAsLong(PlayTokensEntry.COLUMN_GAME_ID));
                    form.setPlayDuration(cv.getAsLong(PlayTokensEntry.COLUMN_PLAY_DURATION));
                    form.setTimestamp(DateUtils.getDateTimeFromTimestamp(cv.getAsString(PlayTokensEntry.COLUMN_TIME_STAMP)));
                    playTokenFormList.add(form);
                }
            } else {
                // Null cursor
                return null;
            }

            // Updates all rows with NONE status to SENT.
            ContentValues statusSentCV = new ContentValues(1);
            statusSentCV.put(PlayTokensEntry._STATUS, StatusColumn.SENT);
            context.getContentResolver().update(
                    PlayTokensEntry.CONTENT_URI,
                    statusSentCV,
                    equal(PlayTokensEntry._STATUS, StatusColumn.NONE),
                    null);

            rowsUploaded = playTokenFormList.size();

            final PlayTokenListWrapper listWrapper = new PlayTokenListWrapper();
            listWrapper.setList(playTokenFormList);

            MobileApiEndpointRequest<Void> request = endpoint.playtokens().send(listWrapper);
            request.execute();
            return request;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            ContentValues cv = new ContentValues(1);
            cv.put(PlayTokensEntry._STATUS, StatusColumn.SUCCESS);
            // delete the uploaded tokens.
            int rowsDeleted = context.getContentResolver().delete(
                    PlayTokensEntry.CONTENT_URI,
                    equal(PlayTokensEntry._STATUS, StatusColumn.SENT),
                    null);
//            if (rowsUploaded != rowsDeleted) {
//                throw new IllegalStateException("Number rows uploaded doesn't match number of rows deleted");
//            }
        }

        @Override
        protected void onFailure(HandlerService context, int resultCode) {
            // Updates all rows with SENT status to FAILED status.
            ContentValues cv = new ContentValues(1);
            cv.put(PlayTokensEntry._STATUS, StatusColumn.FAILED);
            context.getContentResolver().update(
                    PlayTokensEntry.CONTENT_URI,
                    cv,
                    equal(PlayTokensEntry._STATUS, StatusColumn.SENT),
                    null);
        }
    }

}
