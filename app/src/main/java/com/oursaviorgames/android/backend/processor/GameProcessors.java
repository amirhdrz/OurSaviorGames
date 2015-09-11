package com.oursaviorgames.android.backend.processor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameListForm;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.BackendResponseHelper;
import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.data.BaseGameColumns;
import com.oursaviorgames.android.data.ExtraGamesHelper;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.metastore.MetaStore;
import com.oursaviorgames.android.util.DateUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Processors for /games endpoint.
 */
public class GameProcessors {

    public GameProcessors() {
        // required no-arg constructor.
    }

    /**
     * Retrieves explorer game list.
     */
    public static class GameList extends EndpointProcessor<Void> {

        // Server query parameters.
        private static final String PARAM_NEXT_PAGE_TOKEN = "token";

        // 'sort' parameter values.
        public static final String SORT_VALUE_NEW     = "NEW";
        public static final String SORT_VALUE_POPULAR = "POPULAR";

        // Request parameters.
        /**
         * Sort order. One of SORT_VALUE_ constants.
         */
        public static final String PARAM_SORT_ORDER = "sort";

        /**
         * Load more?
         * <P>Type: boolean</P>
         */
        public static final String PARAM_LOAD_MORE = "more";

        private final String sortOrder;
        private final boolean loadMore;
        private final Uri contentUri;

        private GameCollectionResponse response;

        public GameList(Bundle reqParams) {
            super(reqParams);
            sortOrder = checkNotNull(reqParams.getString(PARAM_SORT_ORDER));
            loadMore = reqParams.getBoolean(PARAM_LOAD_MORE, false);
            if (SORT_VALUE_NEW.equals(sortOrder)) {
                contentUri = GameContract.NewGameEntry.CONTENT_URI;
            } else if (SORT_VALUE_POPULAR.equals(sortOrder)) {
                contentUri = GameContract.HotGameEntry.CONTENT_URI;
            } else {
                throw new IllegalStateException("Sort order (" + sortOrder + ") is not valid");
            }
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            //do not perform anything if there's nothing to perform.
            MetaStore store = MetaStore.getMetaStore(context);
            // For now both lists rely on one meta for their last_updated status.
            if (DateUtils.isThresholdPassed(
                    store.getMeta(GameContract.NewGameEntry.META_LAST_UPDATED),
                    context.getResources().getInteger(R.integer.game_list_fetch_wait_time))) {

                // Prepares the request.
                MobileApiEndpointRequest<GameCollectionResponse> request = endpoint.games().list(sortOrder);

                // Set next page token?
                if (loadMore) {
                    String nextPageToken = null;
                    if (SORT_VALUE_NEW.equals(sortOrder)) {
                        nextPageToken = store.getMeta(GameContract.NewGameEntry.META_NEXT_PAGE_TOKEN);
                    } else if (SORT_VALUE_POPULAR.equals(sortOrder)) {
                        nextPageToken = store.getMeta(GameContract.HotGameEntry.META_NEXT_PAGE_TOKEN);
                    }
                    request.put(PARAM_NEXT_PAGE_TOKEN, nextPageToken);
                }

                // Executes the request.
                response = request.execute();
                return request;
            }
            return null;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            if (response != null) {
                // TODO: have to differentiate between the case token is null or not.
                if (!loadMore) {
                    // We're not loading more. Clear cache, since it's a fresh batch.
                    context.getContentResolver().delete(contentUri, null, null);
                }

                final MetaStore store = MetaStore.getMetaStore(context);
                // Store the next page token.
                if (SORT_VALUE_NEW.equals(sortOrder)) {
                    store.putMeta(GameContract.NewGameEntry.META_NEXT_PAGE_TOKEN, response.getNextPageToken());
                } else if (SORT_VALUE_POPULAR.equals(sortOrder)) {
                    store.putMeta(GameContract.HotGameEntry.META_NEXT_PAGE_TOKEN, response.getNextPageToken());
                }

                // Updates the database.
                insertGamesToDb(context, contentUri, response);
            }
        }

        /**
         * Helper method for inserting games into database.
         * @param context
         * @param contentUri
         */
        private static void insertGamesToDb(Context context, Uri contentUri, GameCollectionResponse gameCollection) {
            ContentValues[] cvArray = BackendResponseHelper.gameCollectionResponseToContentValues(gameCollection);
            if (cvArray == null) {
                // null collection
                return;
            }
            context.getContentResolver().bulkInsert(contentUri, cvArray);
        }
    }

    /**
     * Retrieves a single game
     */
    public static class Get extends EndpointProcessor<GameCollectionResponse> {

        /**
         * ArrayList of game ids.
         * Should not be null.
         */
        public static final String PARAM_GAME_IDS= "games";

        private ArrayList<String> gameIds;
        private GameCollectionResponse response = null;

        public Get(Bundle reqParams) {
            super(reqParams);
            gameIds = reqParams.getStringArrayList(PARAM_GAME_IDS);
            checkNotNull(gameIds, "Null gameIds");
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            // Creates each gameId to Long.
            List<Long> gameIdsLong = new ArrayList<>(gameIds.size());
            for (String gameId : gameIds) {
                gameIdsLong.add(Long.valueOf(gameId));
            }
            GameListForm form = new GameListForm();
            form.setGameIds(gameIdsLong);

            // Creates request and executes it.
            MobileApiEndpointRequest<GameCollectionResponse> request = endpoint.games().get(form);
            response = request.execute();
            return request;
        }

        @Override
        protected GameCollectionResponse onGetResult() {
            return response;
        }

    }

    /**
     * Gets a random game.
     */
    public static class RandomGame extends EndpointProcessor<GameResponse> {

        private GameResponse result;

        public RandomGame(Bundle reqParams) {
            super(reqParams);
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            MobileApiEndpointRequest<GameResponse> request = endpoint.games().random();
            result = request.execute();
            return request;
        }

        @Override
        protected GameResponse onGetResult() {
            return result;
        }

    }

    /**
     * Spotlight game conditions:
     * <p>
     *     - Refreshed whenever threshold time passes
     *     - Always different from last played game.
     * </p>
     */
    public static class GetSpotlight extends EndpointProcessor<Void> {

        private static final String TAG = makeLogTag(GetSpotlight.class);

        public static final String PARAM_FORCE_UPDATE = "force_update";

        private boolean forceUpdate;
        private GameResponse response;

        public GetSpotlight(Bundle reqParams) {
            super(reqParams);
            forceUpdate = reqParams.getBoolean(PARAM_FORCE_UPDATE, false);
        }

        @Override
        protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
            MetaStore store = MetaStore.getMetaStore(context);

            final List<Long> gamesToExclude = gamesToExclude(context);

            ContentValues spotlightGame = ExtraGamesHelper.getExtraGame(context, ExtraGamesHelper.SPOTLIGHT_GAME);
            if (spotlightGame != null) {
                String spotlightGameId = spotlightGame.getAsString(BaseGameColumns.COLUMN_GAME_ID);
                if (gamesToExclude.contains(Long.valueOf(spotlightGameId))) {
                    LOGD(TAG, "processRequest:: spotlight game matches last played game");
                    forceUpdate = true;
                }
            }

            // Whether spotlight game should be refreshed
            boolean spotlightGameStale = DateUtils.isThresholdPassed(
                    store.getMeta(ExtraGamesHelper.META_SPOTLIGHT_LAST_UPDATE),
                    context.getResources().getInteger(R.integer.spotlight_game_fetch_wait_time));

            if (forceUpdate || spotlightGameStale) {
                LOGD(TAG, "...processRequest:: excluding games:" + gamesToExclude.toString());
                MobileApiEndpointRequest<GameResponse> request = endpoint.games().spotlight().setExclude(gamesToExclude);
                response = request.execute();
                return request;
            }
            LOGD(TAG, "nothing updated");
            return null;
        }

        /**
         * Returns list of games to exclude in the request.
         * @param context
         * @return A non-null list of games to exclude.
         */
        private static List<Long> gamesToExclude(HandlerService context) {
            List<Long> exclude = new ArrayList<>();

            Cursor cursor = context.getContentResolver().query(
                    GameContract.PlayHistoryEntry.CONTENT_URI,
                    new String[] {GameContract.PlayHistoryEntry.COLUMN_GAME_ID},
                    null,
                    null,
                    GameContract.PlayHistoryEntry.COLUMN_LAST_PLAYED + " DESC"
            );

            if (cursor != null) {
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    // Gets the last played game.
                    final String gameId = cursor.getString(cursor.getColumnIndex(GameContract.PlayHistoryListEntry.COLUMN_GAME_ID));
                    exclude.add(Long.valueOf(gameId));
                } else {
                    // If the last played game is empty,
                    // we should try to exclude the mock last_played_game.
                    ContentValues values = ExtraGamesHelper.getExtraGame(context, ExtraGamesHelper.MOCK_LAST_PLAYED_GAME);
                    if (values != null) {
                        final String gameId = values.getAsString(BaseGameColumns.COLUMN_GAME_ID);
                        exclude.add(Long.valueOf(gameId));
                    }
                }
                cursor.close();
            }

            LOGD(TAG, "Games to exclude: " + exclude.toString());

            return exclude;
        }

        @Override
        protected void onSuccess(HandlerService context) {
            if (response != null) {
                // Updates the last update time.
                MetaStore store = MetaStore.getMetaStore(context);
                store.putMeta(ExtraGamesHelper.META_SPOTLIGHT_LAST_UPDATE, Calendar.getInstance().getTime());
                // Puts the response into database.
                ContentValues values = new ContentValues();
                BackendResponseHelper.gameResponseToContentValues(values, response);
                ExtraGamesHelper.putExtraGame(context, values, ExtraGamesHelper.SPOTLIGHT_GAME);
            }
        }

    }



}

