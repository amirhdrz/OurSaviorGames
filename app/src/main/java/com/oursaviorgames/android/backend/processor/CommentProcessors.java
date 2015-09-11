package com.oursaviorgames.android.backend.processor;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.CommentCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.CommentResponse;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.BackendResponseHelper;
import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.data.StatusColumn;
import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.util.DateUtils;
import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.data.SqlUtils.and;
import static com.oursaviorgames.android.data.SqlUtils.equal;
import static com.oursaviorgames.android.data.SqlUtils.leq;
import static com.oursaviorgames.android.data.SqlUtils.neq;
import static com.oursaviorgames.android.data.SqlUtils.or;
import static com.oursaviorgames.android.util.DateUtils.getShiftedTime;
import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

public class CommentProcessors {

    public CommentProcessors() {
        // required no-arg constructor.
    }

    /**
     * Gets list of comments for a game.
     * <p>
     * {@link #PARAM_GAME_ID} Game id to get comments for. Must not be null.
     * {@link #PARAM_NEXT_PAGE_TOKEN} Token for next page of results.
     * if null request returns first page of results.
     *
     * {@link #onGetResult()} returns next page token or null if there are no more result pages.
     */
    public static class Get extends EndpointProcessor<String> {

        private static final String TAG = makeLogTag(Get.class);

        public static final String PARAM_GAME_ID = "game_id";
        public static final String PARAM_NEXT_PAGE_TOKEN = "token";

        private final String gameId;
        private final String nextPageToken;

        private CommentCollectionResponse response;
        private String result = null;

        public Get(Bundle reqParams) {
            super(reqParams);

            gameId = reqParams.getString(PARAM_GAME_ID);
            nextPageToken = reqParams.getString(PARAM_NEXT_PAGE_TOKEN);

            checkNotNull(gameId, "Null gameId");
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            MobileApiEndpointRequest<CommentCollectionResponse> request =
                    endpoint.comments().list(Long.valueOf(gameId)).setNextPageToken(nextPageToken);
            response = request.execute();
            return request;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            // If nextPageToken is null, this is a first time query,
            // delete all existing comments from the database.
            if (nextPageToken == null) {
                final int timeToKeepFakeComment = context.getResources().getInteger(R.integer.time_to_keep_fake_comment);
                LOGD(TAG, "onSuccess:: deleting previous comments for gameId " + gameId);
                LOGD(TAG, "onSuccess:: shitfted time: " + getShiftedTime(-1 * timeToKeepFakeComment));

                context.getContentResolver().delete(
                        CommentsEntry.CONTENT_URI,
                        and(
                                equal(CommentsEntry.COLUMN_GAME_ID, String.valueOf(gameId)),
                                or(
                                        and(
                                                neq(CommentsEntry._STATUS, StatusColumn.SENT), // don't delete comment with SENT status OR
                                                neq(CommentsEntry._STATUS, StatusColumn.SUCCESS)),
                                        leq(CommentsEntry.COLUMN_TIMESTAMP, getShiftedTime(-1 * timeToKeepFakeComment)) )), // if comment was sent more than 5 mins ago.
                        null);
            }

            // If there are any comments returned, add to database.
            if (response != null && response.getItems() != null) {
                LOGD(TAG, "onSuccess:: insert " + response.getItems().size() + " comments");
                result = response.getNextPageToken();
                insertCommentsToDb(context, response.getItems());
            }
        }

        @Override
        protected void onFailure(HandlerService context, int resultCode) {
        }

        @Override
        protected String onGetResult() {
            return result;
        }

        private static void insertCommentsToDb(Context context, List<CommentResponse> comments) {
            if (comments == null) {
                throw new IllegalArgumentException("Null comments");
            }

            Vector<ContentValues> cvVector = new Vector<>(comments.size());

            for (CommentResponse comment : comments) {
                ContentValues cv = new ContentValues();
                BackendResponseHelper.commentResponseToContentValues(cv, comment);
                cvVector.add(cv);
            }

            if (cvVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);
                context.getContentResolver().bulkInsert(CommentsEntry.CONTENT_URI, cvArray);
            }
        }
    }

    /**
     * Posts comment.
     */
    public static class Post extends EndpointProcessor<Void> {

        public static final String PARAM_GAME_ID = "game_id";
        public static final String PARAM_COMMENT = "comment";

        private final String gameId;
        private final String comment;

        private Uri mPlaceHolderComment;
        private CommentResponse response;

        public Post(Bundle reqParams) {
            super(reqParams);

            gameId = reqParams.getString(PARAM_GAME_ID);
            comment = reqParams.getString(PARAM_COMMENT);

            checkNotNull(gameId, "Null gameId");
            checkNotNull(comment, "Null comment");

        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            // Insert placeholder comment into the database.
            ContentValues cv = new ContentValues(8);
            cv.put(CommentsEntry.COLUMN_COMMENT_ID, new Random().nextInt()); //  inserting with random comment id.
            cv.put(CommentsEntry.COLUMN_GAME_ID, gameId);
            cv.put(CommentsEntry.COLUMN_TIMESTAMP, DateUtils.getCurrentZuluTime());
            cv.put(CommentsEntry.COLUMN_AUTHOR_ID, UserAccount.getUserAccount(context).getUserId());
            cv.put(CommentsEntry.COLUMN_AUTHOR_USERNAME, UserAccount.getUserAccount(context).getUsername());
            cv.put(CommentsEntry.COLUMN_AUTHOR_PROFILE_IMAGE, FileUtils.toUri(FileUtils.getProfilePictureFile(context)).toString());
            cv.put(CommentsEntry.COLUMN_MESSAGE, comment);
            cv.put(CommentsEntry._STATUS, StatusColumn.SENT);
            mPlaceHolderComment = context.getContentResolver().insert(CommentsEntry.CONTENT_URI, cv);

            // Makes http request.
            MobileApiEndpointRequest<CommentResponse> request = endpoint.comments().post(Long.valueOf(gameId), comment);
            response = request.execute();
            return request;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            if (response == null) throw new IllegalStateException("Null CommentResponse");
            ContentValues updatedValues = new ContentValues(1);
            updatedValues.put(CommentsEntry.COLUMN_COMMENT_ID, response.getCommentId());
            updatedValues.put(CommentsEntry._STATUS, StatusColumn.SUCCESS);
            context.getContentResolver().update(mPlaceHolderComment, updatedValues, null, null);
        }

        @Override
        protected void onFailure(final HandlerService context, int resultCode) {
            ContentValues updatedValues = new ContentValues(1);
            updatedValues.put(CommentsEntry._STATUS, StatusColumn.FAILED);
            context.getContentResolver().update(mPlaceHolderComment, updatedValues, null, null);
            context.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.error_send_comment, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        protected Void onGetResult() {
            return null;
        }
    }

    /**
     * Delete a comment.
     */
    public static class Delete extends EndpointProcessor<Void> {

        /**
         * Comment Id.
         * <P> Type: long </P>
         */
        public static final String PARAM_COMMENT_ID = "comment_id";

        private final Long commentId;

        public Delete(Bundle reqParams) {
            super(reqParams);
            if (!reqParams.containsKey(PARAM_COMMENT_ID)) {
                throw new IllegalStateException("Missing comment id");
            }
            commentId = reqParams.getLong(PARAM_COMMENT_ID);
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            MobileApiEndpointRequest<Void> request = endpoint.comments().delete(commentId);
            request.execute();
            return request;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            // delete the comment from the cached database.
            context.getContentResolver().delete(CommentsEntry.CONTENT_URI,
                    equal(CommentsEntry.COLUMN_COMMENT_ID, commentId),
                    null);
        }

        @Override
        protected void onFailure(final HandlerService context, int resultCode) {
            // Shows toast with error message.
            context.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.error_delete_comment, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        protected Void onGetResult() {
            return null;
        }
    }

    /**
     * Flaggs comment as inappropriate.
     */
    public static class Flag extends EndpointProcessor<Void> {

        public static final String PARAM_COMMENT_ID = "comment_id";

        private final long commentId;

        public Flag(Bundle reqParams) {
            super(reqParams);
            if (!reqParams.containsKey(PARAM_COMMENT_ID)) {
                throw new IllegalStateException("Missing comment id");
            }
            commentId = reqParams.getLong(PARAM_COMMENT_ID);
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            MobileApiEndpointRequest<Void> request = endpoint.comments().flagInappropriate(commentId);
            request.execute();
            return request;
        }

        @Override
        protected void onSuccess(final HandlerService context) {
            context.getMainHandler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, R.string.comment_flagged, Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        protected void onFailure(HandlerService context, int resultCode) {
            //TODO: what should we do here.
        }

        @Override
        protected Void onGetResult() {
            return null;
        }
    }
}