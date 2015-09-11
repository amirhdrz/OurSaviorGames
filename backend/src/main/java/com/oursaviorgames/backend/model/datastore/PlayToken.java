package com.oursaviorgames.backend.model.datastore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.condition.IfFalse;
import com.googlecode.objectify.condition.PojoIf;

import java.util.Date;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

/**
 * This entity represents a single play token, containing information
 * about a single user play of a single game.
 * <br>
 * <b>Datastore info:</b>
 * Indexes: {@code gameId}.
 */
@Entity
public class PlayToken {

    public static final String F_GameId = "EF_GameId";
    public static final String F_Counted = "EF_Counted";

    @Id
    private Long   EF_Id;
    @Index(IfNotCounted.class)
    private Long   EF_GameId;
    @Index(IfFalse.class)
    private boolean EF_Counted;
    private Key<UserProfile> EF_User; // the user who generated this playtoken.
    private Date   EF_Timestamp;
    private long   EF_PlayDuration; // play duration in seconds.
    private String EF_RemoteAddr; // IP address of the client or the last proxy that sent the request.

    @SuppressWarnings("unused")
    private PlayToken() {
    }

    public PlayToken(PlayTokenForm form, Key<UserProfile> user, String remoteAddr) {
        EF_Id = null;
        EF_GameId = checkNotNull(form.gameId, "Null gameId");
        EF_PlayDuration = checkNotNull(form.playDuration, "Null play duration");
        EF_Timestamp = checkNotNull(form.getTimestamp(), "Null timestamp");
        EF_Counted = false;
        EF_RemoteAddr = checkNotNull(remoteAddr, "Null remote address");
        EF_User = checkNotNull(user, "Null user");
    }

    public Long getId() {
        return EF_Id;
    }

    public long getGameId() {
        return EF_GameId;
    }

    /**
     * Returns play duration in seconds.
     */
    public long getPlayDuration() {
        return EF_PlayDuration;
    }

    public boolean isCounted() {
        return EF_Counted;
    }

    public void setCounted() {
        EF_Counted = true;
    }

    public Date getTimeStamp() {
        return EF_Timestamp;
    }

    public String getUserRemoteAddr() {
        return EF_RemoteAddr;
    }

    public Key<UserProfile> getUser() {
        return EF_User;
    }

    /**
     * PlayToken form.
     */
    public static class PlayTokenForm {

        private Long gameId;
        private Long playDuration; // Play duration in seconds.
        private Date timestamp;

        public PlayTokenForm() {
        }

        public long getGameId() {
            return gameId;
        }

        public void setGameId(long gameId) {
            this.gameId = gameId;
        }

        public long getPlayDuration() {
            return playDuration;
        }

        public void setPlayDuration(long playDuration) {
            this.playDuration = playDuration;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Create partial index if PlayToken is not counted.
    private static class IfNotCounted extends PojoIf<PlayToken> {
        @Override
        public boolean matchesPojo(PlayToken pojo) {
            return !pojo.EF_Counted;
        }
    }

}
