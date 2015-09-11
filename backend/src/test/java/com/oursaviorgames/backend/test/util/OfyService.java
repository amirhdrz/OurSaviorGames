package com.oursaviorgames.backend.test.util;

import com.oursaviorgames.backend.test.Trivial;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Custom Objectify Service that this application should use.
 * 
 * @author amir
 *
 */
public class OfyService {
	
	/**
	 * Guarantee entity registration with Objectify.
	 */
	static {
		factory().register(Trivial.class);
		//TODO add all entities going to datastore here
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
