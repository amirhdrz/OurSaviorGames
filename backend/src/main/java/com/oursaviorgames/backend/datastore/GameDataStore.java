package com.oursaviorgames.backend.datastore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;

import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.response.GameResponse;

import static com.oursaviorgames.backend.service.OfyService.ofy;

public class GameDataStore {


    public static GameResponse getGameBean(Key<Game> key) throws NotFoundException{
        Game game = ofy().load().key(key).now();
        Developer developer = null;
        if (game != null) {
            developer = ofy().load().key(game.getDeveloperKey()).now();
            if (developer != null) {
                return new GameResponse(game, developer);
            }
        }
        throw new NotFoundException(key);
    }

}
