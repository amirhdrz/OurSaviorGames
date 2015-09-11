package com.oursaviorgames.backend.auth;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;

import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.service.UserService;
import com.oursaviorgames.backend.utils.LogUtils;
import com.oursaviorgames.backend.utils.MemcacheUtils;
import com.oursaviorgames.backend.utils.TimeUtils;

import static com.oursaviorgames.backend.utils.LogUtils.DEBUG_LOGI;

/**
 * Authenticator for users.
 * Authentication is based on the HTTP headers from the client.
 */
public class UserAuthenticator implements Authenticator {

    private static final String TAG = LogUtils.makeLogTag(UserAuthenticator.class);

    private static final String MEMCACHE_NAMESPACE = "ValidatedUsers";

    @Override
    public User authenticate(Authorization authorization) {

        MemcacheService memcache = MemcacheUtils.getSyncMemcacheWithLog(MEMCACHE_NAMESPACE);

        final User user = (User) memcache.get(getCacheKey(authorization));

        // If user is not cached, try authentication against the identity provider.
        if (user != null) {
            DEBUG_LOGI(TAG, "authenticated user cache hit");
            // Cache hit.
            return user;
        } else { // Cache miss.
            DEBUG_LOGI(TAG, "authenticated user cache miss");
            TokenDebugger tokenDebugger = new TokenDebugger();
            final ValidatedToken validatedToken = tokenDebugger.authenticate(
                    authorization.getIdentityProvider(), authorization.getAccessToken());
            // If debugging token against identity provider succeeds.
            if (validatedToken != null) {

                DEBUG_LOGI(TAG, "Access token validated");

                // Access token is valid.
                // Checks if the user this access token belongs to is already registered.
                final String identityProviderUserId = validatedToken.getDebugResponse().getUserId();
                final IdentityProvider identityProvider = validatedToken.getIdentityProvider();

                // Tries to get the user profile from the datastore,
                // either using the userId or querying the database.
                UserService userService = UserService.UserServiceFactory.createInstance();
                UserProfile userProfile;
                if (authorization.getUserId() != null) {
                    DEBUG_LOGI(TAG, "using 'X-UserId' to find the user");
                    long userId = authorization.getUserId();
                    userProfile = userService.getUserProfile(UserProfile.createKey(userId));
                } else {
                    DEBUG_LOGI(TAG, "querying the datastore to find the user");
                    userProfile = userService.queryForExistingUser(identityProvider, identityProviderUserId);
                }

                // A UserProfile has been found on the record.
                if (userProfile != null) {
                    // Checks if the user identityProviderUserId matches the id
                    // returned by TokenDebugger the reason for this is if the user
                    // is only queried using 'X-UserId', we have to make sure the
                    // access token actually belongs to that user.
                    // This operation is redundant when using UserService.queryForExistingUser().
                    if (userProfile.getAuthorityUserId().equals(validatedToken.getDebugResponse().getUserId())) {
                        DEBUG_LOGI(TAG, "user found, authentication complete");
                        User authenticatedUser = new User(userProfile.getId());
                        Expiration expiration = Expiration.onDate(TimeUtils.getDateFromUnixTime(validatedToken.getDebugResponse().getExpiryTime()));
                        memcache.put(getCacheKey(authorization), authenticatedUser, expiration);
                        return authenticatedUser;
                    }
                }
            }
        }
        // Authentication has failed.
        return null;
    }

    /**
     * Returns memcache key based on 'Authorization' header.
     * @param authorization
     * @return
     */
    private static String getCacheKey(Authorization authorization) {
        return authorization.getAccessToken();
    }

}
