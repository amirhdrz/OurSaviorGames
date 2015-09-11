package com.oursaviorgames.backend.model.datastore;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfNotZero;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.oursaviorgames.backend.utils.Preconditions;
import com.oursaviorgames.backend.utils.TimeUtils;

/**
 * Comment entity. Represents a single user comment.
 */
@Entity
public class Comment implements Serializable {

    /**
     * Entity field names.
     */
    public static final String F_Game                   = "EF_Game";
    public static final String F_Timestamp              = "EF_Timestamp";
    public static final String F_InappropriateFlagCount = "EF_InappropriateFlagCount";

    /* Entity fields */
    @Id     Long                  EF_Id;
    @Index  Key<Game>             EF_Game;
    @Index  Date                  EF_Timestamp;
            Key<UserProfile>      EF_Author;
            String                EF_Message;
            Set<Key<UserProfile>> EF_InappropriateFlagList = new HashSet<>();
    @Index(IfNotZero.class) int   EF_InappropriateFlagCount = 0;

    /* Enforced limits on entity fields */
    public static final int MAX_MESSAGE_LENGTH  = 2500;

    /**
     * Required empty constructor for objectify.
     */
    public Comment() {
    }

    public Comment(Key<UserProfile> userKey, Key<Game> gameId, String message)
            throws IllegalArgumentException {
        EF_Author = Preconditions.checkNotNull(userKey, "User key cannot be null");
        EF_Game = Preconditions.checkNotNull(gameId, "Game key cannot be null");
        EF_Message = Preconditions.checkNotNull(message, "Comment message cannot be null");
        if (message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Comment message length cannot be greater than " + MAX_MESSAGE_LENGTH);
        }
        EF_Timestamp = TimeUtils.getCurrentTime();
        EF_Id = null;
        EF_InappropriateFlagCount = 0;
    }

    public long getId() {
        return EF_Id;
    }

    public Date getTimestamp() {
        return EF_Timestamp;
    }

    public long getAuthorId() {
        return EF_Author.getId();
    }

    public String getMessage() {
        return EF_Message;
    }

    public int getFlagInappropriateCount() {
        return EF_InappropriateFlagCount;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<Game> getGameKey() {
        return EF_Game;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<UserProfile> getAuthorKey() {
        return EF_Author;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<Key<UserProfile>> getInappropriateFlagUserList() {
        return EF_InappropriateFlagList;
    }

    public void setMessage(String newMessage) {
        EF_Message = newMessage;
    }

    /**
     * Adds user to set of users who flagged this comment as inappropriate.
     * @param userKey The user flagging the comment.
     * @return true if this set already did not contain this user.
     */
    public boolean addUserToFlagInappropriateList(Key<UserProfile> userKey) {
        boolean result = EF_InappropriateFlagList.add(userKey);
        EF_InappropriateFlagCount = EF_InappropriateFlagList.size();
        return result;
    }

    public static Key<Comment> createKeyFromId(long id) {
        return Key.create(Comment.class, id);
    }



}
