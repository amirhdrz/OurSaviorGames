package com.oursaviorgames.backend.service;

import com.google.appengine.api.datastore.ReadPolicy;
import com.google.appengine.api.memcache.MemcacheService;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.oursaviorgames.backend.CacheNameSpace;
import com.oursaviorgames.backend.datastore.GameDataStore;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.response.GameResponse;

import static com.oursaviorgames.backend.service.OfyService.ofy;

/**
 * Extra games service.
 * Restricted to a thousand games.
 */
public interface ExtraGameService {

    public static final String CACHE_NAMESPACE = CacheNameSpace.EXTRA_GAME_SERVICE_NAMESPACE;

    public static final int ITEM_LIMIT = 1000; //this is totally random number

    /**
     * Service Factory.
     */
    public static class ExtraGameServiceFactory {

        /**
         * Returns a new instance of {@link ExtraGameService} implementation.
         */
        public static ExtraGameService getInstance() {
            return new ExtraGameServiceImpl();
        }
    }

    /**
     * Returns a random game.
     * @return a random game or null if there was an error.
     */
    public GameResponse getRandomGame();

    /**
     * Returns a random game that is not in exclude.
     * @param exclude Games to exclude, or null to exclude no games.
     * @return spotlight game or null if there was an error.
     */
    public GameResponse getSpotlightGame(List<Long> exclude);

    /**
     * Refreshes the cache used by ExtraGameService.
     * Admins should call this function whenever Game entity table changes.
     */
    public void forceRecache();


    //////////////////////////////////////////////////////////
    // ExtraGameService implementation
    //////////////////////////////////////////////////////////
    /**
     * {@link ExtraGameService} implementation.
     */
    static class ExtraGameServiceImpl extends CachedService implements ExtraGameService {

        private static final String GAME_LIST_CACHE_KEY = "game_id_list";

        private static final Random sRandomGenerator = ThreadLocalRandom.current();

        public ExtraGameServiceImpl() {
            super(CACHE_NAMESPACE);
        }

        @Override
        public GameResponse getRandomGame() {
            final List<Key<Game>> gameList = getGameList();
            if (gameList != null && gameList.size() > 0) {
                final int randIndex = sRandomGenerator.nextInt(gameList.size());
                try {
                    return GameDataStore.getGameBean(gameList.get(randIndex));
                } catch (NotFoundException e) {
                    //ignore.
                }
            }
            return null;
        }

        //TODO: need proper algorithm optimization (do some amortized analysis).

        /**
         * So far implemented with an unanalyzed algorithm,
         * tries to get a random game for 10 times, and then gives up.
         * @param exclude Games to exclude, can be null.
         * @return
         */
        @Override
        public GameResponse getSpotlightGame(List<Long> exclude) {
            final List<Key<Game>> gameList = getGameList();

            if (gameList != null && gameList.size() > 0) {
                final int gameListSize = gameList.size();

                int randIndex = sRandomGenerator.nextInt(gameListSize);
                long randomGameId = gameList.get(randIndex).getId();

                if (exclude != null) {
                    int retries = 10;
                    while (retries > 0 && exclude.contains(randomGameId)) {
                        randIndex = sRandomGenerator.nextInt(gameListSize);
                        randomGameId = gameList.get(randIndex).getId();
                        retries--;
                    }
                }

                return GameDataStore.getGameBean(gameList.get(randIndex));
            }
            return null;
        }

        /**
         * Doesn't force a recache until
         */
        @Override
        public void forceRecache() {
            getMemcache().delete(GAME_LIST_CACHE_KEY);
        }

        /**
         * Returns game list, first from cache,
         * if cache is empty, data is fetched from datastore
         * and put into cache.
         */
        @SuppressWarnings("unchecked")
        public List<Key<Game>> getGameList() {
            // Get identifiable.
            final MemcacheService.IdentifiableValue identifiable = getMemcache().getIdentifiable(GAME_LIST_CACHE_KEY);
            List<Key<Game>> gameList;
            if (identifiable == null) {
                // Cache is empty,
                // perform query.
                gameList = getGameListFromDatastore();
                // Puts result into memcache.
                getMemcache().put(
                        GAME_LIST_CACHE_KEY,
                        gameList,
                        null,
                        MemcacheService.SetPolicy.ADD_ONLY_IF_NOT_PRESENT
                );
            } else {
                gameList = (List<Key<Game>>) identifiable.getValue();
            }
            return gameList;
        }

        /**
         * Fetches game list from Datastore.
         * With eventual consistency.
         */
        public List<Key<Game>> getGameListFromDatastore() {
            return ofy()
                    .consistency(ReadPolicy.Consistency.EVENTUAL)
                    .load()
                    .type(Game.class)
                    .filter(Game.F_IsPublished, true)
                    .limit(ITEM_LIMIT)
                    .keys()
                    .list();
        }

    }

}
