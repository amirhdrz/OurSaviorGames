package com.oursaviorgames.backend.test;

import static com.oursaviorgames.backend.test.util.OfyService.ofy;

import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;

/**
 * Tests of projection query operations
 * 
 */
public class QueryProjectionTests {

	Trivial triv = new Trivial(123L, "foo", 42L);
	Key<Trivial> trivKey;

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			// Our tests assume strong consistency
			new LocalDatastoreServiceTestConfig(),
			new LocalMemcacheServiceTestConfig(),
			new LocalTaskQueueTestConfig());

    Closeable objectifyCloseable;

	@Before
	public void setUp() {
		helper.setUp();
        objectifyCloseable = ObjectifyService.begin();

		trivKey = ofy().save().entity(triv).now();
		ofy().clear();
	}

	@After
	public void tearDown() {
        objectifyCloseable.close();
		helper.tearDown();
	}

	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(QueryProjectionTests.class.getName());

	@Test
	public void simpleProjectionWorks() throws Exception {
		List<Trivial> projected = ofy().load().type(Trivial.class)
				.project("someString").list();
		assert projected.size() == 1;

		Trivial pt = projected.get(0);
		assert pt.getId() == triv.getId();
		assert pt.getSomeString().equals(triv.getSomeString());
		assert pt.getSomeNumber() == 0; // default value
	}

	@Test
	public void simpleProjectionWithInt() throws Exception {
		List<Trivial> projected = ofy().load().type(Trivial.class)
				.project("someNumber").list();
		assert projected.size() == 1;

		 Trivial pt = projected.get(0);
		 assert pt.getSomeNumber() == 42L;
	}

	@Test
	public void projectionDoesNotContaminateSession() throws Exception {
		List<Trivial> projected = ofy().load().type(Trivial.class)
				.project("someString").list();
		assert projected.size() == 1;
		assert !ofy().isLoaded(trivKey);

		Trivial fetched = ofy().load().key(trivKey).now();
		assert fetched.getSomeNumber() == triv.getSomeNumber();
	}
}