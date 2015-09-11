package com.oursaviorgames.backend.test;

import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalSocketServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.oursaviorgames.backend.auth.AuthorizationHeaders;
import com.oursaviorgames.backend.auth.IdentityProvider;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.model.response.CommentCollectionResponse;
import com.oursaviorgames.backend.model.response.CommentResponse;
import com.oursaviorgames.backend.service.CommentService;
import com.oursaviorgames.backend.spi.MobileApiEndpoint;

import static com.oursaviorgames.backend.service.OfyService.factory;
import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.test.TestUtils.randomString;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Ignore("need valid access tokens")
public class CommentsEndpointTest {

    // Change access token each time.
    private final static String           ACCESS_TOKEN      = TestUtils.ACCESS_TOKEN;
    private final static IdentityProvider IDENTITY_PROVIDER = TestUtils.IDENTITY_PROVIDER;

    private final static String           ACCESS_TOKEN_2      = TestUtils.ACCESS_TOKEN_2;
    private final static IdentityProvider IDENTITY_PROVIDER_2 = TestUtils.IDENTITY_PROVIDER_2;

    final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig()
                            // We can't handle eventual consistency for Admin related endpoints.
                            .setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
                    new LocalMemcacheServiceTestConfig(),
                    new LocalUserServiceTestConfig(),
                    new LocalSocketServiceTestConfig());

    final MobileApiEndpoint   endpoint = new MobileApiEndpoint();

    Closeable objectifyCloseable;

    // Mock http request.
    HttpServletRequest request;
    HttpServletRequest request2; // request from a second user.

    UserProfile user;
    UserProfile user2;
    Game        game;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void setUp() throws BadRequestException, ForbiddenException, UnauthorizedException, ConflictException {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();

        // Creates mock http request object.
        request = createMock(HttpServletRequest.class);
        expect(request.getHeader(AuthorizationHeaders.AUTHORIZATION)).andReturn(ACCESS_TOKEN).anyTimes();
        expect(request.getHeader(AuthorizationHeaders.IDENTITY_PROVIDER)).andReturn(IDENTITY_PROVIDER.getDomain()).anyTimes();
        expect(request.getHeader(AuthorizationHeaders.USER_ID)).andReturn(null).anyTimes();
        replay(request);

        request2 = createMock(HttpServletRequest.class);
        expect(request2.getHeader(AuthorizationHeaders.AUTHORIZATION)).andReturn(ACCESS_TOKEN_2).anyTimes();
        expect(request2.getHeader(AuthorizationHeaders.IDENTITY_PROVIDER)).andReturn(IDENTITY_PROVIDER_2.getDomain()).anyTimes();
        expect(request2.getHeader(AuthorizationHeaders.USER_ID)).andReturn(null).anyTimes();
        replay(request2);

        user = endpoint.userSignUp(request, "username", UserProfile.Gender.FEMALE, "name", "device_id");
        user2 = endpoint.userSignUp(request2, "username2", UserProfile.Gender.MALE, "name2", "deviced_id");
        game = addGame();
    }

    @After
    public void tearDown() {
        objectifyCloseable.close();
        helper.tearDown();
    }

    /** Basic test to check if HttpServletRequest */
    @Test
    public void testAddUser() throws BadRequestException, ForbiddenException, UnauthorizedException, ConflictException {
        assertNotNull(user);
        assertEquals("username", user.getUsername());
        assertEquals("name", user.getName());
        assertEquals(UserProfile.Gender.FEMALE, user.getGender());
        assertEquals(IDENTITY_PROVIDER, user.getIdentityProvider());
    }

    @Test
    public void testPostCommentWithInvalidGame() throws UnauthorizedException, BadRequestException, ConflictException, ForbiddenException {
        expected.expect(BadRequestException.class);
        expected.expectMessage("Game with id(123) does not exist");

        endpoint.postComment(request, 123l, "something");
    }

    @Test
    public void testPostComment() throws UnauthorizedException, BadRequestException, ConflictException, ForbiddenException {
        endpoint.postComment(request, game.getId(), "new comment");

        List<Comment> commentList = ofy().load().type(Comment.class).list();
        assertEquals(1, commentList.size());

        Comment comment = commentList.get(0);
        assertNotNull(comment);
        assertEquals(Key.create(UserProfile.class, user.getId()), comment.getAuthorKey());
    }

    @Test
    public void testPostCommentInvalidMessage() throws BadRequestException, ForbiddenException, UnauthorizedException, ConflictException {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Comment message cannot be null");

        endpoint.postComment(request, game.getId(), null);

        List<Comment> comments = ofy().load().type(Comment.class).list();
        assertEquals(0, comments.size());
    }

    @Test
    public void testPostCommentMessageTooLong() throws BadRequestException, ForbiddenException, UnauthorizedException, ConflictException {
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Comment message length cannot be greater than " + Comment.MAX_MESSAGE_LENGTH);
        new Comment(user.getKey(), game.getKey(), randomString(Comment.MAX_MESSAGE_LENGTH + 1));
    }

    @Test
    public void testPostGetManyCommentsForOneGame() throws BadRequestException, ForbiddenException, UnauthorizedException, ConflictException, NotFoundException {
        // Insert 1000 comments for a game
        final int TOTAL_COMMENTS = CommentService.PAGES_TO_CACHE * CommentService.COMMENTS_PER_PAGE * 10;
        for (int i = 0; i < TOTAL_COMMENTS; i++) {
            Comment comment = new Comment(user.getKey(), game.getKey(), randomString(50));
            ofy().save().entities(comment).now();
        }

        // Verify all entities have been saved
        List<Comment> commentList = ofy().load().type(Comment.class).list();
        assertEquals(TOTAL_COMMENTS, commentList.size());
        commentList = null;

        List<Collection<CommentResponse>> commentCollectionResponse = new ArrayList<>();
        String nextPageToken = null;
        // First we get the cached pages.
        for (int i = 0; i < CommentService.PAGES_TO_CACHE; i++) {
            CommentCollectionResponse response = endpoint.getComments(game.getId(), nextPageToken);
            nextPageToken = response.getNextPageToken();
            commentCollectionResponse.add(response.getItems());

            assertNotNull(response.getItems());
            assertNotNull(response.getNextPageToken());
            assertEquals(CommentService.COMMENTS_PER_PAGE, response.getItems().size());
            if (i < CommentService.PAGES_TO_CACHE - 1) {
                assertEquals("page" + (i + 1), response.getNextPageToken());
            }
            assertEquals(CommentService.COMMENTS_PER_PAGE, response.getItems().size());
        }
        // Query rest of the comments using datastore cursors.
        while (nextPageToken != null) {
            CommentCollectionResponse response = endpoint.getComments(game.getId(), nextPageToken);
            nextPageToken = response.getNextPageToken();
            commentCollectionResponse.add(response.getItems());
            if (nextPageToken != null) {
                assertFalse(nextPageToken.startsWith("page"));
            }
            assertNotEquals(0, response.getItems().size());
        }

        int totalCount = 0;
        for (Collection<CommentResponse> comments : commentCollectionResponse) {
            totalCount += comments.size();
        }

        assertEquals(TOTAL_COMMENTS, totalCount);
    }

    @Test
    public void testEditComment() {

    }

    // Tests that users cannot edit other users comments.
    @Test
    public void testEditCommentIllegalUser() {

    }

    @Test
    public void testEditIllegalCommentId() {

    }

    @Test
    public void testDeleteComment() {

    }

    @Test
    public void testDeleteCommentIllegalUser() {

    }

    @Test
    public void testDeleteCommentIllegalCommentId() {

    }

    /*
        Helper methods.
     */
    private Game addGame() {
        Key<Developer> developerKey = factory().allocateId(Developer.class);
        Key<Game> gameKey = factory().allocateId(Game.class);
        Developer developer = new Developer(developerKey.getId(), "devname", "devemail", "devwebsite");
        Game game = new Game(gameKey.getId(), developerKey, "game_title");
        ofy().save().entities(developer, game).now();

        assertEquals(1, ofy().load().type(Game.class).list().size());
        assertEquals(1, ofy().load().type(Developer.class).list().size());
        return game;
    }

}
