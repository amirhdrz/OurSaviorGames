package com.oursaviorgames.backend.test.testdata;

import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;

import static com.oursaviorgames.backend.service.OfyService.factory;
import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.test.TestUtils.randomString;
import static org.junit.Assert.assertEquals;

/**
 * LocalServiceTestHelper should be setup correctly before the functions here are used.
 */
public class TestGameData {

    /**
     * Adds devCount * gamesPerDeveloper Games to datastore.
     * ObjectifyService should be active before calling this function.
     */
    public static void addGamesToDataStore(final int devCount, final int gamesPerDeveloper) {
        // Adds a hundred developers
        List<Key<Developer>> developerKeys = new ArrayList<>();
        for (int i = 0; i < devCount; i++) {
            final Key<Developer> key = factory().allocateId(Developer.class);
            developerKeys.add(key);
            ofy().save().entity(new Developer(key.getId(), randomString(20), randomString(20), randomString(20))).now();
        }
        // Adds games for each developer
        for (int i = 0; i < devCount; i++) {
            for (int j = 0; j < gamesPerDeveloper; j++) {
                final long id = factory().allocateId(Game.class).getId();
                final Game game = new Game(id, developerKeys.get(0), randomString(20), randomString(20));
                ofy().save().entity(game).now();
            }
        }
    }

    /**
     * Publishes at most count games in the datastore.
     * @param count
     */
    public static void publishGames(int count) {
        List<Key<Game>> gameKeys = ofy().load().type(Game.class).keys().list();
        int publishCount = Math.min(count, gameKeys.size());
        for (int i = 0; i < publishCount; i++) {
            Game game = ofy().load().key(gameKeys.get(i)).now();
            game.setPublished(true);
            ofy().save().entity(game).now();
        }
    }

}
