package com.oursaviorgames.backend.service;

import com.oursaviorgames.backend.model.datastore.Admin;
import com.oursaviorgames.backend.model.datastore.Comment;
import com.oursaviorgames.backend.model.datastore.Developer;
import com.oursaviorgames.backend.model.datastore.Feedback;
import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.PlaySnapshot;
import com.oursaviorgames.backend.model.datastore.PlayToken;
import com.oursaviorgames.backend.model.datastore.UserProfile;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Objectify Service that this application should use.
 */
public class OfyService {
	
	/**
	 * Guarantee entity registration with Objectify.
	 */
	static {
		factory().register(Game.class);
		factory().register(Developer.class);
		factory().register(Feedback.class);
        factory().register(Admin.class);
        factory().register(PlaySnapshot.class);
        factory().register(PlayToken.class);
        factory().register(Comment.class);
        factory().register(UserProfile.class);
	}
	
	/**
	 * Returns Objectify service.
	 * Use this method to ensure all entities are registered
	 * before using Objectify.
	 * @return Objectify service object.
	 */
	public static Objectify ofy() {
		return ObjectifyService.ofy();
	}
	
	/**
	 * Returns Objectify service factory.
	 * @return ObjectifyFactory.
	 */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

}
