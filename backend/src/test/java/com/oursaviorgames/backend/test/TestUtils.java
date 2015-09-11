package com.oursaviorgames.backend.test;

import java.util.List;

import com.oursaviorgames.backend.auth.IdentityProvider;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.test.util.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestUtils {

    public final static String           ACCESS_TOKEN      = "";
    public final static IdentityProvider IDENTITY_PROVIDER = IdentityProvider.FACEBOOK;

    public final static String           ACCESS_TOKEN_2      = "";
    public final static IdentityProvider IDENTITY_PROVIDER_2 = IdentityProvider.GOOGLE_PLUS;

    public static String randomString(int count) {
        return random(count, 0, 0, true, false, null);
    }

    /**
     * Objectify should be initialized before calling this function.
     */
    public static void assertNumberOfEntities(int expectedCount, Class entity) {
        final List entities = ofy().load().type(entity).list();
        assertNotNull(entities);
        assertEquals(expectedCount, entities.size());
    }

}
