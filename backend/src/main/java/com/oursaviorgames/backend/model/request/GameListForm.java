package com.oursaviorgames.backend.model.request;

import java.util.List;

public class GameListForm {

    List<Long> gameIds;

    @SuppressWarnings("unused")
    private GameListForm(){
        // required empty constructor
    };

    public GameListForm(List<Long> gameIds) {
        this.gameIds = gameIds;
    }

    public List<Long> getGameIds() {
        return gameIds;
    }

}
