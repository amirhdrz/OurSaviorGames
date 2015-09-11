package com.oursaviorgames.backend.model.response;

import java.util.Date;

import com.oursaviorgames.backend.memcache.Cachable;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.UserProfile;

/**
 * Comment client response object.
 */
public class CommentResponse implements Cachable {

    // Fields from Comment entity.
    public final long commentId;
    public final long gameId;
    public final Date timestamp;
    public final long authorId;
    public final String message;
    public final boolean flagged;

    // Fields from user entity.
    public final String authorName;
    public final String authorUsername;
    public final String authorThumbUrl;

    /**
     *
     * @param comment Comment to create response from.
     * @param author Author of the comment.
     * @param flagged  whether this comment is considered a flagged comment or not.
     */
    public CommentResponse(Comment comment, UserProfile author, boolean flagged) {
        commentId = comment.getId();
        gameId = comment.getGameKey().getId();
        timestamp = comment.getTimestamp();
        authorId = comment.getAuthorKey().getId();
        message = comment.getMessage();
        authorName = author.getName();
        authorUsername = author.getUsername();
        authorThumbUrl = author.getProfileThumbUrl();
        this.flagged = flagged;
    }

}
