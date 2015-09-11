package com.oursaviorgames.backend.utils;

import com.googlecode.objectify.Key;

import static com.oursaviorgames.backend.service.OfyService.ofy;

/**
 * Utility functions dealing with datastore keys.
 */
public class DatastoreUtils {

    /**
     * Does a key-only query to determine if an entity with given {@code key} exists.
     * <p>
     * This function blocks.
     *
     * @param key Datastore key to check.
     * @return true if an entity with this key exists, false otherwise.
     */
    public static boolean doesEntityExist(Key key) {
        return !ofy().load().filterKey(key).keys().list().isEmpty();
    }

    /**
     * Prepends '-' for descending sort on an entity field.
     *
     * @param entityField Name of the entity field.
     * @return Sort condition in descending.
     */
    //TODO: move this and some filter functions to a class of its own.
    public static String descSort(String entityField) {
        return "-" + entityField;
    }

}
