package com.oursaviorgames.backend.model.request;

import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.backend.model.datastore.PlayToken;

/**
 *
 */
public class PlayTokenListWrapper{

    private List<PlayToken.PlayTokenForm> playTokens = new ArrayList<>();

    private PlayTokenListWrapper() {
    }

    /**
     * Adds a playtoken to the list.
     * @param playToken
     */
    public void add(PlayToken.PlayTokenForm playToken) {
        this.playTokens.add(playToken);
    }

    /**
     * Returns the list of play-tokens.
     * @return
     */
    public List<PlayToken.PlayTokenForm> getList() {
        return this.playTokens;
    }
}
