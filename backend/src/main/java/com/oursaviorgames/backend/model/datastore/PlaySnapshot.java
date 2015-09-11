package com.oursaviorgames.backend.model.datastore;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

import com.oursaviorgames.backend.utils.TimeUtils;

/**
 * Represents an aggregated snapshot of {@link PlayToken}s.
 *
 * <b>Datastore info:</b>
 * Index: {@code timeStamp}.
 */
@Entity
public class PlaySnapshot {

    @Id
    private Long EF_Id;
    @Index
    private Date EF_TimeStamp;
    private long EF_GameId;
    private int EF_PlayCount;
    private long EF_PlayDuration;

    @SuppressWarnings("unused")
    private PlaySnapshot() {
    }

    public PlaySnapshot(long gameId) {
        this.EF_GameId = gameId;
        this.EF_TimeStamp = TimeUtils.getCurrentTime();
        this.EF_PlayCount = 0;
        this.EF_PlayDuration = 0l;
    }

    public Long getId() {
        return EF_Id;
    }

    public long getGameId() {
        return EF_GameId;
    }

    public Date getTimeStamp() {
        return EF_TimeStamp;
    }

    public int getPlayCount() {
        return EF_PlayCount;
    }

    public long getPlayDuration() {
        return EF_PlayDuration;
    }

    /**
     * Increments play count by one and play duration by {@code playDuration}.
     * @param playDuration
     */
    public void updateCounter(long playDuration) {
        this.EF_PlayCount += 1l;
        this.EF_PlayDuration += playDuration;
    }



}
