package com.oursaviorgames.backend.service;

import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.datastore.ReadPolicy;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.backend.CacheNameSpace;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.model.response.CommentResponse;
import com.oursaviorgames.backend.spi.TxResult;
import com.oursaviorgames.backend.utils.DatastoreUtils;

import static com.oursaviorgames.backend.service.OfyService.ofy;

/**
 * Comment Service.
 */
//TODO: users should not be able to post comments on games that are not published.
public interface CommentService {

    public class CommentServiceFactory {
        /**
         * Returns new instance of {@code CommentService}.
         * @return
         */
        public static CommentService createInstance() {
            return new CommentServiceImpl();
        }

    }

    /**
     * Configuration values and constants.
     */
    public static final int    COMMENTS_PER_PAGE = 30;
    public static final int    PAGES_TO_CACHE    = 5;
    public static final String CACHE_NAMESPACE   = CacheNameSpace.COMMENT_SERVICE_NAMESPACE;

    /**
     * Returns a page of comments for game with {@code gameKey}.
     * Number of items returned depends on the implementation.
     * @param nextPageToken Token for the next page.
     * @param gameKey The key of the game to return comments for.
     * @return A page of comments.
     * @throws NotFoundException Thrown if no such game exists.
     * @throws InvalidPageTokenException Thrown if {@code nextPageToken} is invalid.
     */
    public Page<CommentResponse> getComments(String nextPageToken, Key<Game> gameKey)
            throws NotFoundException, InvalidPageTokenException;

    /**
     * Puts a comment into the datastore.
     * @param userKey Authenticated user posting the comment.
     * @param gameKey Game to which this comment belongs to.
     * @param message Comment message.
     * @throws NotFoundException If game with {@code gameKey} is not found.
     */
    public CommentResponse postComment(Key<UserProfile> userKey, Key<Game> gameKey, String message)
            throws NotFoundException;

    /**
     * Edits the comment with key {@code commentKey} belonging to user
     * with key {@code userKey}.
     * @param userKey Authenticated user editing the comment.
     * @param commentKey The comment being edited.
     * @param newMessage The new comment message.
     * @throws NotFoundException Thrown if no comment with {@code commentKey} was found.
     * @throws ForbiddenException Thrown if user with key {@code userKey} is not authorized
     *                              to edit comment with key {@code commentKey}.
     */
    public void editComment(Key<UserProfile> userKey, Key<Comment> commentKey, final String newMessage)
            throws NotFoundException, ForbiddenException;

    /**
     * Deletes comment with key {@code commentKey}, belonging to user
     * with key {@code userKey}.
     * @param userKey Authenticated user deleting the comment.
     * @param commentKey The comment to be deleted.
     * @throws NotFoundException No comment with key {@code commentKey} was found.
     * @throws ForbiddenException User with key {@code userKey} is not authorized to
     *                              delete Comment with key {@code commentKey}.
     */
    public void deleteComment(Key<UserProfile> userKey, Key<Comment> commentKey)
            throws NotFoundException, ForbiddenException;

    /**
     * Flags a comment as inappropriate.
     * Does not recache.
     * @param userKey Authenticated user doing the flagging.
     * @param commentKey The comment to flag.
     * @return true if this set already did not contain this user.
     * @throws NotFoundException Thrown if no such comment is found.
     */
    public boolean flagComment(Key<UserProfile> userKey, Key<Comment> commentKey)
    throws NotFoundException;

    /**
     * Exposed for admin-purposes only.
     * @param gameKey The game to forceRecache the comment pages. for.
     */
    public void forceRecache(Key<Game> gameKey);

    /**
     * Implementation
     */
    static class CommentServiceImpl extends PagedCachedService<Comment> implements CommentService {

        private static final String QUERY_BUNDLE_GAME_KEY = "gameKey";

        public CommentServiceImpl() {
            super(COMMENTS_PER_PAGE, PAGES_TO_CACHE, CACHE_NAMESPACE);
        }

        @Override
        public Query<Comment> createQueryObject(QueryParams queryParams) {
            return ofy()
                    .consistency(ReadPolicy.Consistency.EVENTUAL)
                    .load()
                    .type(Comment.class)
                    .order(DatastoreUtils.descSort(Comment.F_Timestamp))
                    .filter(Comment.F_Game, queryParams.get(QUERY_BUNDLE_GAME_KEY));
        }

        /** {@inheritDoc}. */
        @Override
        public Page<CommentResponse> getComments(String nextPageToken, Key<Game> gameKey)
                throws NotFoundException, InvalidPageTokenException {
            if (!DatastoreUtils.doesEntityExist(gameKey)) {
                throw new NotFoundException("getComment: no game found with id:" +
                        gameKey.getId());
            }
            // Gets the result from cache or datastore.
            try {
                //TODO: comments should be cached with CommentResponse object. This solution needs to change.
                Page<Comment> commentsPage = getPage(
                        getParentCacheKey(gameKey.getId()),
                        nextPageToken,
                        getQueryParams(gameKey));

                List<CommentResponse> responseList = new ArrayList<>(commentsPage.getItems().size());
                for (Comment comment : commentsPage.getItems()) {
                    UserProfile profile = ofy().load().key(comment.getAuthorKey()).now();
                    boolean flagged = isCommentFlagged(comment.getFlagInappropriateCount());
                    CommentResponse response = new CommentResponse(comment, profile, flagged);
                    responseList.add(response);
                }

                Page<CommentResponse> responsePage = new Page<>();
                responsePage.setItems(responseList);
                responsePage.setToken(commentsPage.getToken());

                return responsePage;
            } catch (IllegalArgumentException e) {
                throw new InvalidPageTokenException("Next page token: " + nextPageToken + " is invalid");
            }
        }

        /** {@inheritDoc} */
        @Override
        public CommentResponse postComment(Key<UserProfile> userKey, Key<Game> gameKey, String message)
                throws NotFoundException, IllegalArgumentException {
            if (!DatastoreUtils.doesEntityExist(gameKey)) {
                throw new NotFoundException("postComment: no game found with id:" +
                        gameKey.getId());
            }
            Comment comment = new Comment(userKey, gameKey, message);
            ofy().save().entity(comment).now();
            // forceRecache the pages for this game.
            forceRecache(gameKey);

            UserProfile author = ofy().load().key(userKey).now();
            if (author == null) throw new IllegalStateException("Null user key " + userKey);
            return new CommentResponse(comment, author, false); // Comment not flagged by default.
        }

        /** {@inheritDoc} */
        @Override
        public void editComment(final Key<UserProfile> userKey, final Key<Comment> commentKey, final String newMessage)
                throws NotFoundException, ForbiddenException {
            TxResult<Key<Game>> txResult = ofy().transact(new Work<TxResult<Key<Game>>>() {
                @Override
                public TxResult<Key<Game>> run() {
                    Comment comment = ofy().load().key(commentKey).now();
                    if (comment == null) {
                        return new TxResult<>(
                                new NotFoundException("No comment found with id:" + commentKey.getId()));
                    }
                    if (comment.getAuthorKey().getId() != userKey.getId()) {
                        return new TxResult<>(
                                new ForbiddenException("User not authorized. UserId:" + userKey.getId()));
                    }

                    comment.setMessage(newMessage);
                    ofy().save().entity(comment).now();
                    return new TxResult<>(comment.getGameKey());
                }
            });
            try {
                Key<Game> gameKey = txResult.getResult();
                // forceRecache the pages for this game.
                forceRecache(gameKey);
            } catch (ConflictException e) {
                // Will not be thrown.
            }
        }

        /** {@inheritDoc} */
        @Override
        public void deleteComment(final Key<UserProfile> userKey, final Key<Comment> commentKey)
                throws NotFoundException, ForbiddenException {
            Comment comment = ofy().load().key(commentKey).now();
            if (comment == null) {
                throw new NotFoundException("No comment found with id:" + commentKey.getId());
            }
            if (comment.getAuthorKey().getId() != userKey.getId()) {
                throw new ForbiddenException("User with id:" + userKey.getId() + " not authorized to delete ");
            }
            ofy().delete().entity(comment).now();
            forceRecache(comment.getGameKey());
        }

        /** {@inheritDoc} */
        @Override
        public boolean flagComment(final Key<UserProfile> userKey, final Key<Comment> commentKey) throws NotFoundException {
            TxResult<Boolean> txResult = ofy().transact(new Work<TxResult<Boolean>>() {
                @Override
                public TxResult<Boolean> run() {
                    Comment comment = ofy().load().key(commentKey).now();
                    if (comment == null) {
                        return new TxResult<>(
                                new NotFoundException("No comment with id(" + commentKey.getId() + ") found."));
                    }
                    boolean success = comment.addUserToFlagInappropriateList(userKey);
                    if (success) {
                        ofy().save().entity(comment).now();
                    }
                    return new TxResult<>(success);
                }
            });
            try {
                return txResult.getResult();
            } catch (ForbiddenException e) {
                //TODO: create a more generic transaction result.
                // Will not be thrown.
                return false;
            } catch (ConflictException e) {
                // Will not be thrown.
                return false;
            }
        }

        /**
         * If more than 5 people have flagged a comment, comment is considered flagged.
         * @param flagCount number of current flag counts on a comment.
         * @return true if comment is considered flagged.
         */
        private boolean isCommentFlagged(int flagCount) {
            return flagCount > 5;
        }

        /** {@inheritDoc} */
        @Override
        public void forceRecache(Key<Game> gameKey) {
            recacheAllPages(getParentCacheKey(gameKey.getId()), getQueryParams(gameKey));

        }

        private static QueryParams getQueryParams(Key<Game> gameKey) {
            QueryParams queryParams = new QueryParams();
            queryParams.put(QUERY_BUNDLE_GAME_KEY, gameKey);
            return queryParams;
        }

        private static String getParentCacheKey(long gameId) {
            return String.valueOf(gameId);
        }

    }

}
