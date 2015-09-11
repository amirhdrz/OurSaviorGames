package com.oursaviorgames.backend.service;

import com.google.api.server.spi.response.ConflictException;
import com.google.appengine.api.datastore.ReadPolicy;
import com.googlecode.objectify.Key;

import java.util.List;
import java.util.Locale;

import com.oursaviorgames.backend.auth.IdentityProvider;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.model.types.ValidatedUsername;
import com.oursaviorgames.backend.model.types.ValidationException;
import com.oursaviorgames.backend.utils.LogUtils;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGE;

/**
 * Service for UserProfiles.
 */
public interface UserService {

    /**
     * Factory for {@link com.oursaviorgames.backend.service.UserService}
     */
    public class UserServiceFactory {

        /**
         * Returns a new instance of {@link UserService} implementation.
         * @return
         */
        public static UserService createInstance() {
            return new UserServiceImpl();
        }

    }

    /**
     * Updates the profile information of user with userKey.
     * @param userKey User profile to update.
     * @param newUsername New user name.
     * @return new UserProfile if it is updated, or returns previous
     *              UserProfile if no changes have been made.
     * @throws UsernameNotUniqueException Thrown if the newUsername is not unique.
     */
    public UserProfile editUserProfile(Key<UserProfile> userKey, ValidatedUsername newUsername)
            throws UsernameNotUniqueException;

    /**
     * Fetches {@link UserProfile} of an authenticated user.
     * <p>
     *     <em>Datastore: loads data with strong consistency.</em>
     *     TODO: is there a penalty for loading data with strong consistency
     * </p>
     * @param userKey Authenticated user.
     * @return {@link UserProfile}, or null if user was not found.
     */
    public UserProfile getUserProfile(Key<UserProfile> userKey);

    /**
     * Registers a new user synchronously.
     * <p>
     *     <em>Datastore: saves with strong consistency</em>
     * </p>
     * @param username Unique username.
     * @param identityProvider Identity provider.
     * @param identityProviderUserId Identity provider user_id.
     * @param gender User's gender.
     * @param name User's name.
     * @param deviceId User's device registration id for push notifications.
     * @return Create UserProfile.
     * @throws UsernameNotUniqueException Thrown if the {@code username} is not unique.
     * @throws ConflictException Thrown if a user with identityProviderUserId already exists.
     */
    public UserProfile registerUser(ValidatedUsername username,
                                    IdentityProvider identityProvider,
                                    String identityProviderUserId,
                                    UserProfile.Gender gender,
                                    String name,
                                    String deviceId)
    throws UsernameNotUniqueException, ConflictException;

    /**
     * Generates a unique username given a user's name.
     * @param name The user's name.
     * @return A unique username or an empty string if there was an error
     *          or if a username could not be generated within 10 tries.
     */
    public String generateUserName(String name);

    /**
     * Queries the database with identityProviderUserId to see if such a user is already registered.
     * @param identityProvider
     * @param identityProviderUserId
     * @return {@link UserProfile} if a user is found, null otherwise.
     */
    public UserProfile queryForExistingUser(IdentityProvider identityProvider, String identityProviderUserId);


    /**
     * Implementation
     */
    class UserServiceImpl implements UserService {

        private static final String TAG = LogUtils.makeLogTag(UserServiceImpl.class);

        @Override
        public UserProfile getUserProfile(Key<UserProfile> userKey) {
            return ofy().consistency(ReadPolicy.Consistency.STRONG)
                    .load()
                    .key(userKey)
                    .now();
        }

        @Override
        public UserProfile editUserProfile(Key<UserProfile> userKey, ValidatedUsername newUsername) throws UsernameNotUniqueException {
            UserProfile profile = ofy().load().key(userKey).now();
            if (profile == null) {
                throw new IllegalStateException("UserProfile for id " + userKey.getId() + " is null");
            }
            if (!profile.getUsername().equals(newUsername.getValue())) {
                // Update username if it is different.
                if (!isUsernameUnique(newUsername.getValue())) {
                    throw new UsernameNotUniqueException("Username(" + newUsername.getValue() + ") is not unique");
                }
                profile.setUsername(newUsername);
                ofy().save().entity(profile).now();
            }
            return profile;
        }

        /** {@inheritDoc} */
        @Override
        public UserProfile registerUser(ValidatedUsername username,
                                        IdentityProvider identityProvider,
                                        String identityProviderUserId,
                                        UserProfile.Gender gender,
                                        String name,
                                        String deviceId)
                throws UsernameNotUniqueException, ConflictException {
            // Checks if user is already registered.
            UserProfile currentUser = queryForExistingUser(identityProvider, identityProviderUserId);
            if (currentUser != null) {
                // If a user with identityProviderUserId already exists.
                throw new ConflictException("User with user_id(" + identityProviderUserId
                        + ") is already registered.");
            } else {
                // Checks if username is unique or throws exception.
                if (!isUsernameUnique(username.getValue())) {
                    throw new UsernameNotUniqueException("Username(" + username.getValue() + ") is not unique.");
                }
                // Creates new entity and stores it into the datastore.
                UserProfile newUserProfile = new UserProfile(
                        username, identityProviderUserId, identityProvider, gender, name, deviceId);
                ofy().consistency(ReadPolicy.Consistency.STRONG)
                        .save()
                        .entity(newUserProfile)
                        .now();
                return newUserProfile;
            }
        }

        /** {@inheritDoc} */
        @Override
        public String generateUserName(String name) {
            if (name == null) {
                // Something has gone horribly wrong.
                // Just return an empty string.
                LOGE(TAG, "Got null name");
                return "";
            }

            String username = name.replaceAll("\\s", "_");
            username = username.toLowerCase(Locale.ROOT);
            if (username.length() > ValidatedUsername.UsernameMaxLength) {
                username = username.substring(0, ValidatedUsername.UsernameMaxLength);
            }
            try {
                ValidatedUsername validatedUsername = new ValidatedUsername(username);
                int i = 0;
                while (!isUsernameUnique(validatedUsername.getValue())) {
                    if (++i > 10) {
                        // A unique username has not been generated within 10 tries,
                        // just return an empty string.
                        return "";
                    }
                    String randomInt = String.valueOf((int) (Math.random() * 10));
                    username = validatedUsername.getValue().concat(randomInt);
                    validatedUsername = new ValidatedUsername(username);
                }
            } catch (ValidationException e) {
                return "";
            }
            return username;
        }

        /** {@inheritDoc} */
        public UserProfile queryForExistingUser(IdentityProvider identityProvider, String identityProviderUserId) {
            List<UserProfile> queryResultList = ofy()
                    .consistency(ReadPolicy.Consistency.STRONG)
                    .load()
                    .type(UserProfile.class)
                    .filter(UserProfile.F_IdentityProviderUserId, identityProviderUserId)
                    .filter(UserProfile.F_IdentityProvider, identityProvider.getDomain())
                    .list();

            if (queryResultList.size() > 1) {
                LOGE(TAG, "Duplicate user with authority id("
                        + identityProviderUserId + ") found.");
            } else if (queryResultList.size() == 1){
                return queryResultList.get(0);
            }
            // All else:
            return  null;
        }

        /**
         * Whether {@code username} is unique.
         * Checks against the datastore with strong consistency.
         * @param username Username to check.
         * @return True if {@code username} is unique, false otherwise.
         */
        private boolean isUsernameUnique(String username) {
            List<UserProfile> userList = ofy().consistency(ReadPolicy.Consistency.STRONG).load()
                    .type(UserProfile.class).filter(UserProfile.F_Username, username).list();
            return (userList.size() == 0);
        }

    }

}