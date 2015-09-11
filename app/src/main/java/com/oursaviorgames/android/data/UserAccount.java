package com.oursaviorgames.android.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.oursaviorgames.android.data.metastore.MetaStore;
import com.oursaviorgames.android.data.metastore.StringMetaKey;

import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Manages user's auth data.
 * Not thread-safe.
 */
//TODO: some of information below should be stored in a json file.
public class UserAccount {

    private static final Uri URI = MetaStore.buildClassUri(UserAccount.class);

    /** MetaStore keys */
    private static final StringMetaKey nameMetaKey     = new StringMetaKey(URI, "name", null);
    private static final StringMetaKey userNameMetaKey = new StringMetaKey(URI, "usename", null);
    private static final StringMetaKey userIdMetaKey   = new StringMetaKey(URI, "userid", null);
    private static final StringMetaKey ipMetaKey       = new StringMetaKey(URI, "ip", null);
    private static final StringMetaKey profileImageKey = new StringMetaKey(URI, "profile_image", null);

    private static UserAccount sUserAccount;

    private MetaStore metaStore;

    public static UserAccount getUserAccount(Context context) {
        if (sUserAccount == null) {
            sUserAccount = new UserAccount(context);
        }
        return sUserAccount;
    }

    private UserAccount(Context context) {
        metaStore = MetaStore.getMetaStore(context);
    }

    /**
     * Returns true if the user has previously sign into the app.
     * Does not guarantee that user's session is still valid.
     * @return
     */
    public boolean isUserSignedIn() {
        String userId = metaStore.getMeta(userIdMetaKey);
        return (userId != null);
    }

    /** Returns internal user id. */
    public String getUserId() {
        return metaStore.getMeta(userIdMetaKey);
    }

    /** Returns the identity provider */
    public IdentityProvider getIdentityProvider() {
        String ip = metaStore.getMeta(ipMetaKey);
        if (ip != null) {
            return IdentityProvider.parseDomain(ip);
        }
        return null;
    }

    /**
     * Returns user's username.
     */
    public String getUsername() {
        return metaStore.getMeta(userNameMetaKey);
    }

    /**
     * Returns user's name.
     */
    public String getName() {
        return metaStore.getMeta(nameMetaKey);
    }

    /**
     * Returns profile image url. Maybe null if non has been set.
     */
    public String getProfileImageUrl() {
        return metaStore.getMeta(profileImageKey);
    }

    /**
     * Call this function when the user has successfully signed in, and provide following data.
     */
    public void setUserSignedIn(String userId,
                                IdentityProvider identityProvider,
                                String username,
                                String name,
                                @Nullable String profileImageUrl) {
        checkNotNull(userId, "Null userId");
        checkNotNull(identityProvider, "Null identityProvider");
        checkNotNull(username, "Null username");
        checkNotNull(name, "Null name");

        metaStore.putMeta(userIdMetaKey, userId);
        metaStore.putMeta(ipMetaKey, identityProvider.getDomain());
        metaStore.putMeta(userNameMetaKey, username);
        metaStore.putMeta(nameMetaKey, name);
        metaStore.putMeta(profileImageKey, profileImageUrl);
    }

    public void updateUserAccount(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username cannot be null");
        }

        metaStore.putMeta(userNameMetaKey, username);
    }

    public void clearAccountData() {
        metaStore.putMeta(userIdMetaKey, null);
        metaStore.putMeta(ipMetaKey, null);
        metaStore.putMeta(userNameMetaKey, null);
        metaStore.putMeta(nameMetaKey, null);
        metaStore.putMeta(profileImageKey, null);
        //TODO: also delete CredentialService data? but is this the right place
    }

    /** The domain values must match the values on server */
    public enum IdentityProvider {
        GOOGLE_PLUS("google.com"),
        FACEBOOK("facebook.com");

        private String domain;

        IdentityProvider(String domain) {
            this.domain = domain;
        }

        /**
         * Returns the domain of this authority.
         *
         * @return
         */
        public String getDomain() {
            return this.domain;
        }

        /**
         * Returns Enum object matching authorityDomain retrieved from
         * a previous call to {@link IdentityProvider#getDomain()}.
         * @param authorityDomain
         * @return
         */
        public static IdentityProvider parseDomain(String authorityDomain) {
            for (IdentityProvider authority : IdentityProvider.values()) {
                if (authority.domain.equals(authorityDomain)) {
                    return authority;
                }
            }
            return null;
        }
    }
}
