package com.oursaviorgames.backend.model.response;

import java.util.Date;

import com.oursaviorgames.backend.memcache.Cachable;
import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;

/**
 * Game client response object.
 */
public final class GameResponse implements Cachable {

    public final long    gameId;
    public final String  title;
    public final String  shortDescription;
    public final Long    developerId;
    public final String  developerName;
    public final int     version;
    public final long    hotScore;
    public final int     playCount;
    public final Date datePublished;
    public final boolean isPlayableOffline;
    public final String  originUrl;

    /**
     *  We only use name and id of the developer,
     *  developer can be a projection.
     * @param game
     * @param developer Projection of developer with id and name.
     */
    public GameResponse(Game game, Developer developer) {
        gameId = game.getId();
        title = game.getTitle();
        shortDescription = game.getShortDescription();
        developerId = developer.getId();
        developerName = developer.getName();
        version = game.getVersion();
        hotScore = game.getHotScore();
        playCount = game.getPlayCount();
        datePublished = game.getDatePublished();
        isPlayableOffline = game.isPlayableOffline();
        originUrl = game.getOriginUrl();
    }

}
