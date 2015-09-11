package com.oursaviorgames.backend.test;


import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

/**
 * This test demonstrates that an exception (java.lang.IllegalArgumentException: Cursor does not
 * match query.) occurs in Objectify 4.1.3 with Google App Engine 1.9.4 when doing keys-only queries
 * with cursors. The only difference between the testEntitiesQuery() and testKeysQuery() is that the
 * testKeysQuery() method uses a keys-only query. Once it's fixed, I think testKeysQuery() will
 * work.
 */
public class KeysOnlyCursorTest {

    private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100)
                    .setNoStorage(true));

    private Key<KeysOnlyCursorTestRootEntity> rootKey;
    private Closeable objectifyCloseable;

    @Before
    public void setUp() {
        helper.setUp();
        objectifyCloseable = ObjectifyService.begin();
        ObjectifyService.register(KeysOnlyCursorTestRootEntity.class);
        ObjectifyService.register(KeysOnlyCursorTestEntity.class);

        rootKey = ofy().save().entity(new KeysOnlyCursorTestRootEntity()).now();
    }

    @After
    public void tearDown() {
        objectifyCloseable.close();
        helper.tearDown();
    }

    /**
     * This test succeeds.
     */
    @Test
    public void testEntitiesQuery() {
        // create test items
        createTestItems();

        // do first query
        final QueryResultIterator<KeysOnlyCursorTestEntity> it1 = ofy().load()
                .type(KeysOnlyCursorTestEntity.class).ancestor(rootKey)
                .filter("testString >", "testString1").limit(3).iterator();
        int i = 2;
        while (it1.hasNext()) {
            final KeysOnlyCursorTestEntity ent = it1.next();
            Assert.assertEquals("testString" + i++, ent.testString);
        }

        // get cursor
        final String cursor = it1.getCursor().toWebSafeString();

        // do second query
        final QueryResultIterator<KeysOnlyCursorTestEntity> it2 = ofy().load()
		.type(KeysOnlyCursorTestEntity.class).ancestor(rootKey)
		.filter("testString >", "testString1").startAt(Cursor.fromWebSafeString(cursor))
		.limit(3).iterator();
	i = 5;
	while (it2.hasNext()) {
	    final KeysOnlyCursorTestEntity ent = it2.next();
	    Assert.assertEquals("testString" + i++, ent.testString);
	}

	// clean up
	cleanUpTestItems();
    }

    /**
     * This test fails (throws java.lang.IllegalArgumentException: Cursor does not match query.).
     */
    @Test
    public void testKeysQuery() {
	// create test items
	createTestItems();

	// do first query
	final QueryResultIterator<Key<KeysOnlyCursorTestEntity>> it1 = ofy().load()
		.type(KeysOnlyCursorTestEntity.class).ancestor(rootKey)
		.filter("testString >", "testString1").limit(3).keys().iterator();
	int i = 2;
	while (it1.hasNext()) {
	    final Key<KeysOnlyCursorTestEntity> entKey = it1.next();
	    Assert.assertEquals("testString" + i++, ofy().load().key(entKey).now().testString);
	}

	// get cursor
	final String cursor = it1.getCursor().toWebSafeString();

	// do second query
	final QueryResultIterator<Key<KeysOnlyCursorTestEntity>> it2 = ofy().load()
		.type(KeysOnlyCursorTestEntity.class).ancestor(rootKey)
		.filter("testString >", "testString1").startAt(Cursor.fromWebSafeString(cursor))
		.limit(3).keys().iterator();
	i = 5;
	// the failure occurs when it2.hasNext() is called
	while (it2.hasNext()) {
	    final Key<KeysOnlyCursorTestEntity> entKey = it2.next();
	    Assert.assertEquals("testString" + i++, ofy().load().key(entKey).now().testString);
	}

	// clean up
	cleanUpTestItems();
    }

    private void cleanUpTestItems() {
	ofy().delete().keys(
		ofy().load().type(KeysOnlyCursorTestEntity.class).ancestor(rootKey).keys());
    }

    private void createTestItems() {
	final ArrayList<KeysOnlyCursorTestEntity> ents = new ArrayList<KeysOnlyCursorTestEntity>();
	for (int i = 1; i <= 9; i++) {
	    ents.add(new KeysOnlyCursorTestEntity(rootKey, "testString" + i));
	}
	ofy().save().entities(ents).now();
    }

}