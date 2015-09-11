package com.oursaviorgames.backend.spi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.oursaviorgames.backend.Constants;
import com.oursaviorgames.backend.auth.User;
import com.oursaviorgames.backend.auth.ValidatedToken;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.Feedback;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.PlayToken;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.model.request.GameListForm;
import com.oursaviorgames.backend.model.types.ValidatedUsername;
import com.oursaviorgames.backend.model.types.ValidationException;
import com.oursaviorgames.backend.model.response.CommentCollectionResponse;
import com.oursaviorgames.backend.model.response.CommentResponse;
import com.oursaviorgames.backend.model.response.GameCollectionResponse;
import com.oursaviorgames.backend.model.response.GameResponse;
import com.oursaviorgames.backend.model.response.SimpleResponse;
import com.oursaviorgames.backend.service.CommentService;
import com.oursaviorgames.backend.service.ExtraGameService;
import com.oursaviorgames.backend.service.GameService;
import com.oursaviorgames.backend.service.UserService;
import com.oursaviorgames.backend.service.InvalidPageTokenException;
import com.oursaviorgames.backend.service.UsernameNotUniqueException;
import com.oursaviorgames.backend.utils.AuthUtils;
import com.oursaviorgames.backend.utils.EmailUtils;
import com.oursaviorgames.backend.utils.LogUtils;
import com.oursaviorgames.backend.service.Page;
import com.oursaviorgames.backend.utils.TimeUtils;
import com.oursaviorgames.backend.model.request.PlayTokenListWrapper;

import static com.oursaviorgames.backend.service.OfyService.factory;
import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGW;

/**
 * Defines APIs to be used by the Android and iOS mobile clients.
 */
@Api(
		name = "mobileApiEndpoint",
		version = "v1",
		scopes = { Constants.EMAIL_SCOPE },
		clientIds = { Constants.ANDROID_CLIENT_ID,
				Constants.API_EXPLORER_CLIENT_ID }, 
		audiences = { Constants.ANDROID_AUDIENCE }, 
		description = "Catalogg mobile backend API"
)
public class MobileApiEndpoint {

    private static final String TAG = LogUtils.makeLogTag(MobileApiEndpoint.class);

    @ApiMethod(name="games.list", path = "games/{sort}", httpMethod = HttpMethod.GET)
    public GameCollectionResponse getGameList(@Named("sort") GameService.SortOrder sortOrder,
                                              @Nullable @Named("token") String nextPageToken)
    throws BadRequestException {
        GameService gameService = GameService.GameServiceFactory.createInstance();
        try {
            switch (sortOrder) {
                case POPULAR:
                    return gameService.getPopularGames(nextPageToken);
                case NEW:
                    return gameService.getNewGames(nextPageToken);
                default:
                    throw new BadRequestException("Sort order (" + sortOrder + ") is invalid");
            }
        } catch (InvalidPageTokenException e) {
            throw new BadRequestException("Token (" + nextPageToken + ") is invalid");
        }
    }

    /**
     * Returns collection containing {@link GameResponse} object for each
     * game specified in gameListForm.
     * <p>
     * Only published games are returned.
     *
     * @param gameListForm Form containing ids of games to get.
     * @return {@link GameCollectionResponse} or null if list of gameIds is null.
     * @throws RequestEntityTooLargeException if number of gameIds is too large. (currently more than 50).
     * @throws BadRequestException if gameListForm is null.
     */
    @ApiMethod(name = "games.get", path = "games", httpMethod = HttpMethod.POST)
    public GameCollectionResponse getGames(GameListForm gameListForm)
            throws RequestEntityTooLargeException, BadRequestException {
        if (gameListForm == null) {
            throw new BadRequestException("gameListForm is null");
        }
        List<Long> gameIds = gameListForm.getGameIds();
        if (gameIds == null) {
            return null;
        }
        if (gameIds.size() > 50) {
            throw new RequestEntityTooLargeException("Maximum of 50 gameIds allowed per request");
        }
        GameService gameService = GameService.GameServiceFactory.createInstance();
        return gameService.getGames(gameIds);
    }

    /**
     * Endpoint for sending feedback.
     * @param message Feedback message.
     * @param email Optional email address.
     */
    @ApiMethod(name = "feedback.send", path = "feedback", httpMethod = HttpMethod.POST)
    public void sendFeedback(@Named("message") final String message,
                             @Nullable @Named("email") String email) {
        long id = factory().allocateId(Feedback.class).getId();
		Feedback fd = new Feedback(id, email, message, TimeUtils.getCurrentTime());
		ofy().save().entities(fd);

        // Email feedback. // something for work for now.
        String text = "Feedback id: " + String.valueOf(id);
        if (email != null) text += "\nUser email: " + email;
        if (message != null)  text += "\nFeedback: " + message;
        EmailUtils.sendAdminEmail("User feedback", text);
	}

    /**
     * Endpoint for submitting a list of play-tokens.
     */
    @ApiMethod(name=  "playtokens.send", path = "playtokens", httpMethod = HttpMethod.POST)
    public void uploadPlayTokens(HttpServletRequest httpRequest, PlayTokenListWrapper playTokenListWrapper)
    throws UnauthorizedException {
        // Authenticates user.
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        // Gets the client ip address.
        String ipAddress = AuthUtils.getIpAddress(httpRequest);
        // Gets the list of play tokens and stores them in datastore.
        List<PlayToken> playTokens = new ArrayList<>(playTokenListWrapper.getList().size());
        for (PlayToken.PlayTokenForm playTokenFrom : playTokenListWrapper.getList()) {
            playTokens.add(new PlayToken(playTokenFrom, UserProfile.createKey(user), ipAddress));
        }
        //TODO: create a playtoken service.
        ofy().save().entities(playTokens);
    }

    //////////////////////////////////////////////
    // UserProfile endpoints
    //////////////////////////////////////////////
    /**
     * Gets the {@link com.oursaviorgames.backend.model.datastore.UserProfile} for the authenticated user.
     * @param httpRequest Http request for authentication.
     * @return {@link com.oursaviorgames.backend.model.datastore.UserProfile} or throws an exception.
     * @throws com.google.api.server.spi.response.UnauthorizedException Thrown if user is not authorized.
     */
    @ApiMethod(name = "users.getProfile", path = "users/profile", httpMethod = ApiMethod.HttpMethod.GET)
    public UserProfile getUserProfile(HttpServletRequest httpRequest)
            throws UnauthorizedException {
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        Key<UserProfile> userKey = UserProfile.createKey(user);
        UserService userService = UserService.UserServiceFactory.createInstance();
        return userService.getUserProfile(userKey);
    }

    /**
     * Generates a unique username.
     * @param httpRequest Http request for debugging access token only.
     * @param name User's User's name.
     * @return {@link com.oursaviorgames.backend.model.response.SimpleResponse} containing the generated username.
     * @throws com.google.api.server.spi.response.ForbiddenException
     */
    @ApiMethod(name = "users.generateUsername", path = "users/generateUsername", httpMethod = ApiMethod.HttpMethod.GET)
    public SimpleResponse generateUserName(HttpServletRequest httpRequest, @Named("name") String name)
            throws ForbiddenException {
        // Debugs the access token from the http request.
        ValidatedToken validatedToken = AuthUtils.debugAccessToken(httpRequest);
        if (validatedToken == null) {
            throw new ForbiddenException("Access token is invalid");
        }
        // Generates username using UserService.
        UserService userService = UserService.UserServiceFactory.createInstance();
        String generatedUsername = userService.generateUserName(name);
        return new SimpleResponse(generatedUsername);
    }

    /**
     * Endpoint for user sign up.
     * @param httpRequest Http request for debugging access token only.
     * @param username New user name to register.
     * @param gender New user's gender.
     * @param name User's name.
     * @return {@link UserProfile} of the new user if successful.
     * @throws ForbiddenException If this user is already registered.
     * @throws com.google.api.server.spi.response.ConflictException Thrown if {@code username} is not unique.
     * @throws BadRequestException Thrown if {@code username} could not be validated
     *                              as a valid username.
     * @throws UnauthorizedException Thrown if access token is invalid.
     */
    @ApiMethod(name = "users.signup", path = "users/signup", httpMethod = ApiMethod.HttpMethod.POST)
    public UserProfile userSignUp(HttpServletRequest httpRequest,
                                  @Named("username") String username,
                                  @Named("gender") UserProfile.Gender gender,
                                  @Named("name") String name,
                                  @Named("deviceId") String deviceId)
            throws ForbiddenException, ConflictException, BadRequestException, UnauthorizedException {
        // Debugs the access token from the http request.
        ValidatedToken validatedToken = AuthUtils.debugAccessToken(httpRequest);
        if (validatedToken == null) {
            throw new UnauthorizedException("Access token is invalid");
        }
        // Registers user.
        try {
            UserService userService = UserService.UserServiceFactory.createInstance();
            ValidatedUsername validatedUsername = new ValidatedUsername(username);
            return userService.registerUser(
                    validatedUsername,
                    validatedToken.getIdentityProvider(),
                    validatedToken.getDebugResponse().getUserId(),
                    gender,
                    name,
                    deviceId
            );
        } catch (ValidationException e) {
            throw new BadRequestException("Username could not be validated");
        } catch (UsernameNotUniqueException e) {
            throw new ConflictException("Provided username is not unique");
        } catch (ConflictException e) {
            throw new ForbiddenException("User is already registered");
        }
    }

    @ApiMethod(name = "users.update", path = "users/update", httpMethod = ApiMethod.HttpMethod.POST)
    public UserProfile updateUsername(HttpServletRequest httpRequest,
                                      @Named("username") String newUsername)
            throws ConflictException, BadRequestException, UnauthorizedException {
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        Key<UserProfile> key = UserProfile.createKey(user);
        UserService userService = UserService.UserServiceFactory.createInstance();
        try {
            ValidatedUsername validatedUsername = new ValidatedUsername(newUsername);
            return userService.editUserProfile(key, validatedUsername);
        } catch (ValidationException e) {
            throw new BadRequestException("username could not be validated");
        } catch (UsernameNotUniqueException e) {
            throw new ConflictException("Provided username is not unique");
        }

    }

    //////////////////////////////////////////////
    // ExtraGames endpoints
    //////////////////////////////////////////////
    /**
     * Returns random game.
     */
    @ApiMethod(name = "games.random", path = "games/random", httpMethod = ApiMethod.HttpMethod.GET)
    public GameResponse getRandomGame()
            throws InternalServerErrorException {
        final ExtraGameService service = ExtraGameService.ExtraGameServiceFactory.getInstance();
        GameResponse game = service.getRandomGame();
        if (game == null) {
            throw new InternalServerErrorException("There was an error retrieving random game");
        }
        return game;
    }

    /**
     * Returns spotlight game, while excluding any games in exclude list.
     * @param exclude Games to exclude.
     */
    @ApiMethod(name = "games.spotlight", path="games/spotlight", httpMethod = ApiMethod.HttpMethod.GET)
    public GameResponse getSpotlightGame(@Nullable @Named("exclude") final List<Long> exclude)
            throws InternalServerErrorException, RequestUriTooLongException {
        if (exclude != null && exclude.size() > 10) {
            throw new RequestUriTooLongException("exclusion list should contain at most 10 items");
        }
        final ExtraGameService service = ExtraGameService.ExtraGameServiceFactory.getInstance();
        GameResponse game = service.getSpotlightGame(exclude);
        if (game == null) {
            throw new InternalServerErrorException("There was an error retrieving spotlight game");
        }
        return game;
    }

    //////////////////////////////////////////////
    // Comments endpoints
    //////////////////////////////////////////////
    /**
     * Endpoint for getting a page of comments of game with {@code gameId}.
     * @param gameId The game to get the comments for.
     * @param nextPageToken A nextPageToken supplied by a previous call to this function,
     *              to get the next page of comments.
     * @return
     */
    @ApiMethod(name = "comments.list", path = "comments", httpMethod = ApiMethod.HttpMethod.GET)
    public CommentCollectionResponse getComments(@Named("gameId") long gameId,
                                                 @Nullable @Named("nextPageToken") String nextPageToken)
            throws NotFoundException, BadRequestException {
        Key<Game> gameKey = Game.createKeyFromId(gameId);
        CommentService commentService = CommentService.CommentServiceFactory.createInstance();
        try {
            Page<CommentResponse> resultPage = commentService.getComments(nextPageToken, gameKey);
            return new CommentCollectionResponse(resultPage.getItems(), resultPage.getToken());
        } catch (NotFoundException e) {
            LOGW(TAG, e.getMessage());
            throw new NotFoundException("Game with id(" + gameId + ") does not exist");
        } catch (InvalidPageTokenException e) {
            LOGW(TAG, e.getMessage());
            throw new BadRequestException("nextPageToken is invalid");
        }
    }

    @ApiMethod(name = "comments.post", path = "comments", httpMethod = ApiMethod.HttpMethod.POST)
    public CommentResponse postComment(HttpServletRequest httpRequest,
                            @Named("gameId") long gameId,
                            @Named("message") String message)
            throws UnauthorizedException, BadRequestException{
        // Authenticates user.
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        // Prepares keys and comment service.
        Key<UserProfile> userKey = UserProfile.createKey(user);
        Key<Game> gameKey = Game.createKeyFromId(gameId);
        CommentService commentService = CommentService.CommentServiceFactory.createInstance();
        // Tries to post the comment.
        try {
            return commentService.postComment(userKey, gameKey, message);
        } catch (NotFoundException e) {
            LOGW(TAG, e.getMessage());
            throw new BadRequestException("Game with id(" + gameId + ") does not exist");
        }
    }

    @ApiMethod(name="comments.edit", path="comments/{commentId}", httpMethod = ApiMethod.HttpMethod.PUT)
    public void editComment(HttpServletRequest httpRequest,
                            @Named("commentId") long commentId,
                            @Named("message") String message)
            throws UnauthorizedException, BadRequestException, ForbiddenException {
        // Authenticates user.
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        // Prepares keys and comment service.
        Key<UserProfile> userKey = UserProfile.createKey(user);
        Key<Comment> commentKey = Comment.createKeyFromId(commentId);
        CommentService commentService = CommentService.CommentServiceFactory.createInstance();
        // Tries to edit the comment.
        try {
            commentService.editComment(userKey, commentKey, message);
        } catch (NotFoundException e) {
            LOGW(TAG, e.getMessage());
            throw new BadRequestException("Comment with id(" + commentId + ") does not exist");
        } catch (ForbiddenException e) {
            LOGW(TAG, e.getMessage());
            throw new ForbiddenException("User is not allowed to edit this comment.");
        }
    }

    @ApiMethod(name="comments.delete", path="comments/{commentId}", httpMethod = ApiMethod.HttpMethod.DELETE)
    public void deleteComment(HttpServletRequest httpRequest, @Named("commentId") long commentId)
            throws UnauthorizedException, BadRequestException, ForbiddenException{
        // Authenticates user.
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        // Prepares keys and comment service.
        Key<UserProfile> userKey = UserProfile.createKey(user);
        Key<Comment> commentKey = Comment.createKeyFromId(commentId);
        CommentService commentService = CommentService.CommentServiceFactory.createInstance();
        // Tries to delete comment.
        try {
            commentService.deleteComment(userKey, commentKey);
        } catch (NotFoundException e) {
            LOGW(TAG, e.getMessage());
            throw new BadRequestException("Comment with id(" + commentId + ") was not found");
        } catch (ForbiddenException e) {
            LOGW(TAG, e.getMessage());
            throw new ForbiddenException("User is not allowed to delete this comment");
        }
    }

    @ApiMethod(name="comments.flagInappropriate", path = "comments/flag/{commentId}", httpMethod = ApiMethod.HttpMethod.POST)
    public void flagCommentAsInappropriate(HttpServletRequest httpRequest,
                                           @Named("commentId") long commentId)
            throws UnauthorizedException, NotFoundException{
        // Authenticates user.
        User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
        // Prepares keys and comment service.
        Key<UserProfile> userKey = UserProfile.createKey(user);
        Key<Comment> commentKey = Comment.createKeyFromId(commentId);
        CommentService commentService = CommentService.CommentServiceFactory.createInstance();
        // Tries to flag comment.
        try {
            commentService.flagComment(userKey, commentKey);
        } catch (NotFoundException e) {
            LOGW(TAG, e.getMessage());
            throw new NotFoundException("Comment with id(" + commentId + ") was not found");
        }
    }

}
