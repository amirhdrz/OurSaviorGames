package com.oursaviorgames.backend.test;

import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.service.ExtraGameService;
import com.oursaviorgames.backend.spi.MobileApiEndpoint;
import com.oursaviorgames.backend.spi.RequestUriTooLongException;
import com.oursaviorgames.backend.test.testdata.TestGameData;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for ExtraGamesEndpointV2
 */
public class ExtraGameServiceEndpointTest {

    final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig()
                            .setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
                    new LocalMemcacheServiceTestConfig()
            );

    Closeable objectifyCloseable;

    // Service endpoint instance
    public final MobileApiEndpoint endpoint = new MobileApiEndpoint();

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
        // Testing datastore is empty before each call.
        testDataStoreEmpty();
    }

    @After
    public void tearDown() {
        objectifyCloseable.close();
        helper.tearDown();
    }

    // Making sure there are no leaked entities.
    @Test
    public void testDataStoreEmpty() {
        // Making sure there are no developers.
        List<Key<Developer>> devKeys = ofy().load().type(Developer.class).keys().list();
        assertEquals(0, devKeys.size());
        // Making sure there are no games.
        List<Key<Game>> gameKeys = ofy().load().type(Game.class).keys().list();
        assertEquals(0, gameKeys.size());
    }

    @Test
    public void testGetRandomGameEmptyDatastore() throws InternalServerErrorException {
        expected.expect(InternalServerErrorException.class);
        expected.expectMessage("There was an error retrieving random game");
        endpoint.getRandomGame();
    }

    @Test
    public void testGetSpotlightGameEmptyDatastore() throws InternalServerErrorException, RequestUriTooLongException {
        expected.expect(InternalServerErrorException.class);
        expected.expectMessage("There was an error retrieving spotlight game");
        endpoint.getSpotlightGame(null);
    }

    @Test
    public void testGetRandomGameNoPublishedGames() throws InternalServerErrorException {
        expected.expect(InternalServerErrorException.class);
        expected.expectMessage("There was an error retrieving random game");
        // Adds unpublished games to the datastore
        TestGameData.addGamesToDataStore(2, 2);
        ExtraGameService.ExtraGameServiceFactory.getInstance().forceRecache();
        endpoint.getRandomGame();
    }

    @Test
    public void testGetSpotlightGameNoPublishedGames() throws InternalServerErrorException, RequestUriTooLongException {
        expected.expect(InternalServerErrorException.class);
        expected.expectMessage("There was an error retrieving spotlight game");
        // Adds unpublished games to datastore.
        TestGameData.addGamesToDataStore(2, 2);
        ExtraGameService.ExtraGameServiceFactory.getInstance().forceRecache();
        endpoint.getSpotlightGame(null);
    }

    @Test
    public void testGetRandomGame() throws InternalServerErrorException {
        // Adds one game to datastore.
        TestGameData.addGamesToDataStore(1, 1);
        TestGameData.publishGames(1);
        TestUtils.assertNumberOfEntities(1, Developer.class);
        TestUtils.assertNumberOfEntities(1, Game.class);

        final Game game = ofy().load().type(Game.class).list().get(0);
        assertNotNull(game);
        assertNotNull(game.getDatePublished());

        ExtraGameService.ExtraGameServiceFactory.getInstance().forceRecache();

        Game.GameBean randomGame = endpoint.getRandomGame();
        assertNotNull(randomGame);
        assertEquals(game.getId(), randomGame.gameId);
    }

    @Test
    public void testGetSpotlightGameNoExclusions() throws InternalServerErrorException, RequestUriTooLongException {
        // Adds one game to datastore.
        TestGameData.addGamesToDataStore(1, 1);
        TestGameData.publishGames(1);
        TestUtils.assertNumberOfEntities(1, Developer.class);
        TestUtils.assertNumberOfEntities(1, Game.class);

        final Game game = ofy().load().type(Game.class).list().get(0);
        assertNotNull(game);
        assertNotNull(game.getDatePublished());

        ExtraGameService.ExtraGameServiceFactory.getInstance().forceRecache();

        Game.GameBean spotlightGame = endpoint.getSpotlightGame(null);
        assertNotNull(spotlightGame);
        assertEquals(game.getId(), spotlightGame.gameId);
    }

    @Test
    public void testGetSpotlightGameTooManyExclusions() throws InternalServerErrorException, RequestUriTooLongException {
        expected.expect(RequestUriTooLongException.class);
        expected.expectMessage("exclusion list should contain at most 10 items");

        final List<Long> exclude = new ArrayList<>(11);
        for (int i = 0; i < 11; i++) {
            exclude.add((long) i);
        }

        endpoint.getSpotlightGame(exclude);
    }

    @Test
    public void testGetSpotlightExclude() throws InternalServerErrorException, RequestUriTooLongException {
        // Adds two games, publishes both
        TestGameData.addGamesToDataStore(2, 1);
        TestGameData.publishGames(2);
        // Checking if there are two published games.
        final List<Game> gameList = ofy().load().type(Game.class).list();
        assertNotNull(gameList);
        assertEquals(2, gameList.size());
        assertNotNull(gameList.get(0).getDatePublished());
        assertNotNull(gameList.get(1).getDatePublished());

        // Exclude index 0
        final long excludedGameId = gameList.get(0).getId();
        final List<Long> exclude = new ArrayList<>(1);
        exclude.add(excludedGameId);

        // The endpoint is random, so we check the condition at least 50 times.
        for (int i = 0; i < 50; i++) {
            Game.GameBean spotlightGame = endpoint.getSpotlightGame(exclude);
            assertNotEquals(spotlightGame.gameId, excludedGameId);
        }
    }

}
