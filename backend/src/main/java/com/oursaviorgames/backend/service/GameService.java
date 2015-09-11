package com.oursaviorgames.backend.service;

import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.oursaviorgames.backend.CacheNameSpace;
import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.response.GameResponse;
import com.oursaviorgames.backend.model.response.GameCollectionResponse;
import com.oursaviorgames.backend.utils.DatastoreUtils;
import com.oursaviorgames.backend.utils.LogUtils;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGD;
import static com.oursaviorgames.backend.utils.LogUtils.LOGE;
import static com.oursaviorgames.backend.utils.LogUtils.LOGW;

/**
 *
 */
public interface GameService {

    public static class GameServiceFactory {

        /**
         * Returns a new instance of {@code GameService}.
         * @return
         */
        public static GameService createInstance() {
            return new GameServiceImpl();
        }

    }

    /**
     * Number of games per page of results.
     */
    public static final int ITEMS_PER_PAGE = 30;

    /**
     * Cache name space for game service.
     */
    public static final String CACHE_NAMESPACE = CacheNameSpace.GAME_SERVICE_NAMESPACE;


    /**
     * Repopulates the {@code GameService} cache,
     * reading from datastore with eventual consistency.
     */
    public void reCache();

    /**
     * Fetches a limited collection of popular games sorted by their hot-score
     * starting from {@code nextPageToken}.
     * @param nextPageToken
     * @return
     * @throws InvalidPageTokenException if {@code nextPageToken} is invalid.
     */
    public GameCollectionResponse getPopularGames(@Nullable String nextPageToken)
            throws InvalidPageTokenException;

    /**
     * Fetches a limited collection of new games sorted by their date-added
     * starting from {@code nextPageToken}.
     * @param nextPageToken
     * @return
     * @throws InvalidPageTokenException if {@code nextPageToken} is invalid.
     */
    public GameCollectionResponse getNewGames(@Nullable String nextPageToken)
            throws InvalidPageTokenException;

    /**
     * Returns collection containing {@link GameResponse} object for each
     * game specified in gameIds.
     * <p>
     * Only published games are returned.
     *
     * @param gameIds Games to get.
     * @return Collection of {@link GameResponse object} or an empty collection or null if gameIds is null.
     */
    public GameCollectionResponse getGames(List<Long> gameIds);

    /**
     * Whether game with key {@code gameKey} is published.
     * @param gameKey
     * @return
     * @throws NotFoundException Throws if no such game is found.
     */
    public boolean isPublishedGame(Key<Game> gameKey) throws NotFoundException;

    /**
     * Game list sort order.
     */
    public static enum SortOrder {
        POPULAR,
        NEW
    }

    /**
     * Implementation
     */
    //TODO: move class to its own file.
    static class GameServiceImpl implements GameService {

        private static final String TAG = LogUtils.makeLogTag(GameServiceImpl.class);

        private MemcacheService memCache;

        public GameServiceImpl() {
            memCache = MemcacheServiceFactory.getMemcacheService(CACHE_NAMESPACE);
            memCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        }

        //TODO: abstract out all memcache functions.
        private static String getCacheKey(GameOrdering ordering, String startCursor) {
            return ordering.name() + "|" + ((startCursor == null) ? "page1" : startCursor);
        }

        //TODO: abstract out all memcache functions.
        //TODO: need a way to refresh the cache. should we set expiration?!
        //TODO: this cache doesn't check if the key is in cache before warming up.
        private void warmUpCache() {
            String cursorStr = null;
            try {
                for (GameOrdering gameOrdering : GameOrdering.values()) {
                    // caches 5 pages of results
                    for (int i = 0; i < 4; i++) {
                        // Gets the old values.
                        MemcacheService.IdentifiableValue identifiable =
                                memCache.getIdentifiable(getCacheKey(gameOrdering, cursorStr));

                        // Creates the new values.
                        GameCollectionResponse games;
                        try {
                            games = fetchGames(gameOrdering, cursorStr);
                        } catch (NotFoundException e) {
                            LOGW(TAG, "No published games found");
                            break;
                        }

                        // Puts the new value in cache.
                        boolean success;
                        if (identifiable != null) {
                            success = memCache.putIfUntouched(getCacheKey(gameOrdering, cursorStr), identifiable, games);
                        } else {
                            memCache.put(getCacheKey(gameOrdering, cursorStr), games);
                            success = true;
                        }
                        if (success) {
                            cursorStr = games.getNextPageToken();
                            // If there is no next page, break out of the loop.
                            if (cursorStr == null) {
                                break;
                            }
                        } else {
                            // abandon warming up.
                            throw new IllegalStateException("someone else is writing to the cache");
                        }
                    }
                }
            } catch (IllegalStateException e) {
                LOGE(TAG, "someone else is writing to cache");
            }
        }

        /**
         * Fetches collection of games from the datastore with strong consistency ordered by ordering.
         *
         * @param ordering sort order
         * @param startCursor start point of the cursor.
         * @return Collection of {@code GameBean}s or null if result set is empty
         * or if an error occurred during the datastore fetch.
         * or if an error occurred during the datastore fetch.
         */
        private GameCollectionResponse fetchGames(GameOrdering ordering, String startCursor)
                throws NotFoundException{
            //TODO: abstract out , datastore fetch codes.
            List<Game> gameList = new ArrayList<Game>(ITEMS_PER_PAGE);
            List<Key<Developer>> devKeyList = new ArrayList<Key<Developer>>(ITEMS_PER_PAGE);

            // Builds query for fetching published games.
            Query<Game> gameQuery = ofy().consistency(ReadPolicy.Consistency.EVENTUAL).load().
                    type(Game.class).order(ordering.getSortOrder())
                    .filter(Game.F_IsPublished, true).chunk(ITEMS_PER_PAGE).limit(ITEMS_PER_PAGE);

            // Sets start cursor and gets iterator from the query.
            if (startCursor != null) {
                gameQuery = gameQuery.startAt(Cursor.fromWebSafeString(startCursor));
            }
            QueryResultIterator<Game> gameIterator = gameQuery.iterator();

            // Iterates over the results from the query.
            // stores projection of games and developer keys.
            int count = ITEMS_PER_PAGE;
            while ((count > 1) && gameIterator.hasNext()) {
                Game game = gameIterator.next();
                gameList.add(game);
                devKeyList.add(game.getDeveloperKey());
                count--;
            }

            if (gameList.size() == 0) {
                throw new NotFoundException("No published games found");
            }

            // Retrieve Developers from the database.
            Map<Key<Developer>, Developer> developers = ofy().load().keys(devKeyList);

            // Creates result collection.
            Collection<GameResponse> resultList = new ArrayList<>(gameList.size());
            for (Game game : gameList) {
                resultList.add(new GameResponse(game, developers.get(game.getDeveloperKey())));
            }

            // Creates response.
            // Sets the next page token.
            String nextPageToken = null;
            if (gameIterator.hasNext()) {
                nextPageToken = gameIterator.getCursor().toWebSafeString();
            }

            return GameCollectionResponse.builder().setItems(resultList)
                    .setNextPageToken(nextPageToken).build();
        }

        @SuppressWarnings("unchecked cast")
        private GameCollectionResponse fetchFromCache(GameOrdering ordering,
                                                      String startCursorStr) {

            GameCollectionResponse response;

            // Gets game list from cache.
            response = (GameCollectionResponse) memCache.get(getCacheKey(ordering, startCursorStr));

            // Checks if data is in cache.
            if (response == null) {
                // Checks if cache is warmed up.
                if (startCursorStr == null) {
                    warmUpCache();
                    response = (GameCollectionResponse) memCache.get(getCacheKey(ordering, null));
                } else {
                    try {
                        response = fetchGames(ordering, startCursorStr);
                        memCache.put(getCacheKey(ordering, startCursorStr), response);
                    } catch (NotFoundException e) {
                        LOGE(TAG, "No games found at supplied cursor: " + startCursorStr);
                    }
                }
            }

            // return collection response.
            return response;
        }

        @Override
        public void reCache() {
            warmUpCache();
        }

        @Override
        public GameCollectionResponse getPopularGames(@javax.annotation.Nullable String nextPageToken) {
            return fetchFromCache(GameOrdering.HOT_SCORE, nextPageToken);
        }

        @Override
        public GameCollectionResponse getNewGames(@javax.annotation.Nullable String nextPageToken) {
            return fetchFromCache(GameOrdering.DATE_PUBLISHED, nextPageToken);
        }

        @Override
        public GameCollectionResponse getGames(List<Long> gameIds) {
            //TODO: performance: is it a good idea to add GameResponse objects to memcache?! How would we deal with cache coherency.
            if (gameIds == null) {
                return null;
            }
            List<GameResponse> response = new ArrayList<>(gameIds.size());
            LOGD(TAG, "getGames:: gameIds.size(): " + gameIds.size());
            for (Long id : gameIds) {
                try {
                    //TODO: performance: partition the list and load in chunks.
                    Game game = ofy().load().key(Game.createKeyFromId(id)).safe();
                    if (!game.isPublished()) {
                        // Skip games that are not published.
                        continue;
                    }
                    Developer dev = ofy().load().key(game.getDeveloperKey()).safe();
                    GameResponse gameResponse = new GameResponse(game, dev);
                    response.add(gameResponse);
                } catch (com.googlecode.objectify.NotFoundException e) {
                    // do nothing.
                }
            }
            return GameCollectionResponse.builder().setItems(response).build();
        }

        @Override
        public boolean isPublishedGame(Key<Game> gameKey) {
            //TODO: implement this function
            return false;
        }

        private static enum GameOrdering {
            DATE_PUBLISHED(DatastoreUtils.descSort(Game.F_DatePublished)),
            HOT_SCORE(DatastoreUtils.descSort(Game.F_HotScore));

            private final String ordering;

            GameOrdering(String ordering) {
                this.ordering = ordering;
            }

            /**
             * Returns sort order on this field.
             * To be used directly in objectify.
             */
            public String getSortOrder() {
                return ordering;
            }
        }

    }

}
