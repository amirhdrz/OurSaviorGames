package com.oursaviorgames.backend.test;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.oursaviorgames.backend.model.datastore.Admin;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Feedback;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.spi.AdminApi;
import com.oursaviorgames.backend.utils.AuthUtils;
import com.oursaviorgames.backend.model.request.DeveloperForm;

import static com.oursaviorgames.backend.service.OfyService.factory;
import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.test.TestUtils.randomString;
import static com.oursaviorgames.backend.test.util.RandomStringUtils.random;
import static com.oursaviorgames.backend.utils.LogUtils.makeLogTag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * General test for AdminApi endpoints.
 * Only simple functionality of AdminApi is tested here,
 * and their side-effects on other services is not taken into consideration.
 */
public class AdminApiTests {

    private static final String TAG = makeLogTag(AdminApiTests.class);

    HashMap<String, Object> envAttr = new HashMap<String, Object>();

    {
        envAttr.put("com.google.appengine.api.users.UserService.user_id_key", "10");
    }

    final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig()
                            // We can't handle eventual consistency for Admin related endpoints.
                            .setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
                    new LocalMemcacheServiceTestConfig(),
                    new LocalUserServiceTestConfig())
                    .setEnvIsAdmin(true)
                    .setEnvIsLoggedIn(true)
                    .setEnvEmail("adminuser@test.com")
                    .setEnvAuthDomain("google.com")
                    .setEnvAttributes(envAttr);

    final AdminApi adminApi = new AdminApi();
    Closeable objectifyCloseable;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
        addAdmin();
    }

    @After
    public void tearDown() {
        objectifyCloseable.close();
        helper.tearDown();
    }

    // First testing if the
    @Test
    public void testIsAdmin() {
        com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();
        assertTrue(AuthUtils.isUserAdmin(userService.getCurrentUser()));
    }

    // Tests that every endpoint in AdminApi can only be used by admins.
    @Test(expected = UnauthorizedException.class)
    public void testOnlyAdmin() throws UnauthorizedException, BadRequestException {
        User invalidUser = new User("adminuser@test.com", "google.com", "11");
        adminApi.addDeveloper(invalidUser, null);
        adminApi.createGame(invalidUser, null, null, null);
        adminApi.publishGame(invalidUser, 0l);
        adminApi.deleteComment(invalidUser, 0l);
        adminApi.getFeedbacks(invalidUser);
        adminApi.getReportedComments(invalidUser);
    }

    // Test adding a single developer to the database.
    @Test
    public void testInsertReadSingleDeveloper() throws UnauthorizedException, BadRequestException {
        // Insert
        DeveloperForm df = new DeveloperForm("devname", "email", "website");
        Developer savedDeveloper = adminApi.addDeveloper(getUser(), df);
        Key savedDeveloperKey = savedDeveloper.getKey();

        // Read
        Developer developer = (Developer) ofy().load().key(savedDeveloperKey).now();
        assertEquals("devname", developer.getName());
        assertEquals("email", developer.getEmail());
        assertEquals("website", developer.getWebsite());
    }

    // Tests inserting and reading a single developer with invalid parameters to the database.
    @Test(expected = BadRequestException.class)
    public void testInsertReadSingleDeveloperInvalid() throws UnauthorizedException, BadRequestException {
        DeveloperForm df = new DeveloperForm(null, null, null);
        adminApi.addDeveloper(getUser(), df);
    }

    // Tests adding developers to the database
    @Test
    public void testAddDevelopers() throws UnauthorizedException, BadRequestException {
        for (int i = 0; i < 100; i++) {
            DeveloperForm df = new DeveloperForm(randomString(50), randomString(50), randomString(50));
            adminApi.addDeveloper(getUser(), df);
        }
        List<Developer> developerList = ofy().load().type(Developer.class).list();
        assertEquals(100, developerList.size());
    }

    // Tests adding a single game with all valid parameters.
    @Test
    public void testAddPublishedGame() throws UnauthorizedException, BadRequestException {
        final Key<Developer> devKey = factory().allocateId(Developer.class);
        final Developer developer = new Developer(devKey.getId(), "dev", "email", "random website");
        ofy().save().entity(developer).now();
        Game game = adminApi.createGame(getUser(),
                devKey.getId(), "random game title", "random game desc");
        assertNull(game.getDatePublished());
        // publishing the game.
        adminApi.publishGame(getUser(), game.getId());
        assertEquals(devKey, game.getDeveloperKey());
        assertNotNull(game.getDatePublished());
        assertEquals(0, game.getHotScore());
        assertEquals(0l, game.getPlayTime());
        assertEquals(0, game.getPlayCount());
        assertEquals("random game desc", game.getShortDescription());
        assertEquals("random game title", game.getTitle());
        assertEquals(0, game.getVersion());
    }

    // Tests adding a game with invalid parameters to the database.
    @Test
    public void testAddGameInvalidDeveloper() throws UnauthorizedException, BadRequestException {
        expected.expect(BadRequestException.class);
        expected.expectMessage("Developer id 123 is invalid");
        adminApi.createGame(getUser(), 123l, "random title", "random desc");
    }

    // Tests adding a lot of games to the datastore.
    @Test
    public void testAddManyGames() throws UnauthorizedException, BadRequestException {
        // Checks developers entity is empty.
        List<Developer> developers = ofy().load().type(Developer.class).list();
        assertEquals(0, developers.size());
        // Adds developers.
        List<Long> developerIds = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DeveloperForm df =  new DeveloperForm(randomString(20), randomString(20), null);
            Developer dev = adminApi.addDeveloper(getUser(), df);
            developerIds.add(dev.getId());
        }
        // Adds games for each developer.
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                adminApi.createGame(getUser(), developerIds.get(i), randomString(10), randomString(10));
            }
        }
        // Checks all the games have been added.
        List<Game> gameList = ofy().load().type(Game.class).list();
        assertEquals(1000, gameList.size());
    }

    @Test
    public void testPublishGameInvalidId() throws UnauthorizedException, BadRequestException {
        expected.expect(BadRequestException.class);
        expected.expectMessage("Game id 123 is not valid");
        adminApi.publishGame(getUser(), 123l);
    }

    @Test
    public void testPublishGame() throws UnauthorizedException, BadRequestException {
        final Developer dev = adminApi.addDeveloper(getUser(), new DeveloperForm("name", "email", "website"));
        final Game game = adminApi.createGame(getUser(), dev.getId(), "game_title", "game_description");
        assertNotNull(game);
        adminApi.publishGame(getUser(), game.getId());
        assertEquals(dev.getKey(), game.getDeveloperKey());
        assertEquals("game_title", game.getTitle());
        assertEquals("game_description", game.getShortDescription());

        // Test by published date.
        Game savedGame = ofy().load().key(game.getKey()).now();
        assertNotNull(savedGame);
        assertEquals(game.getId(), savedGame.getId());
        assertEquals(game.getDatePublished().toString(), savedGame.getDatePublished().toString());

        // Test by query
        List<Game> queriedSavedGame = ofy().load().type(Game.class).filter(Game.F_IsPublished, true).list();
        assertEquals(queriedSavedGame.size(), 1);
        assertEquals(queriedSavedGame.get(0).getKey(), game.getKey());
    }

    @Test
    public void testFeedbacks() throws UnauthorizedException {
        // insert
        for (int i = 0; i < 500; i++) {
            Feedback feedback = new Feedback("email", "message", new Date());
            ofy().save().entity(feedback).now();
        }
        // read
        final CollectionResponse<Feedback> feedbacks = adminApi.getFeedbacks(getUser());
        assertEquals(500, feedbacks.getItems().size());
    }

    @Test
    public void testGetReportedComments() throws UnauthorizedException {
        for (int i = 1; i <= 100; i++) {
            Comment comment = new Comment(Key.create(UserProfile.class, i), Key.create(Game.class, i), "msg " + i);
            if (i % 2 == 0) {
                for (int j = 1; j <= i; j++) {
                    comment.addUserToFlagInappropriateList(Key.create(UserProfile.class, j));
                }
            }
            if (i % 2 == 0) {
                assertEquals(i, comment.getFlagInappropriateCount());
            } else {
                assertEquals(0, comment.getFlagInappropriateCount());
            }
            ofy().save().entity(comment).now();
        }

        List<Comment> commentList = ofy().load().type(Comment.class).list();
        assertEquals(100, commentList.size());

        List<Comment> flaggedComments = ofy().load().type(Comment.class).filter(Comment.F_InappropriateFlagCount + " !=", 0).list();
        assertEquals(50, flaggedComments.size());

        CollectionResponse response = adminApi.getReportedComments(getUser());
        assertEquals(50, response.getItems().size());
    }

    @Test
    public void testDeleteComment() throws UnauthorizedException {
        for (int i = 1; i <= 100; i++) {
            Comment comment = new Comment(Key.create(UserProfile.class, 1312l), Key.create(Game.class, 132l), "some random message");
            ofy().save().entity(comment).now();
        }

        List<Comment> commentList = ofy().load().type(Comment.class).list();
        assertEquals(100, commentList.size());

        for (int i = 0; i < 10; i++) {
            adminApi.deleteComment(getUser(), commentList.get(i).getId());
        }

        List<Comment> commentListNew = ofy().load().type(Comment.class).list();
        assertEquals(90, commentListNew.size());
    }

    /**
     * Returns current user from {@link com.google.appengine.api.users.UserService}
     */
    com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();
    private User getUser() {
        return userService.getCurrentUser();
    }

    /** Adds admin entity to datastore */
    private void addAdmin() {
        User user = getUser();
        Admin admin = new Admin(user.getUserId(), "AdminUser", "adminuser@test.com");
        ofy().save().entity(admin).now();
    }

}
