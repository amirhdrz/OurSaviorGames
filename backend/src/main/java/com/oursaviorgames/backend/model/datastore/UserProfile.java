package com.oursaviorgames.backend.model.datastore;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import com.oursaviorgames.backend.auth.IdentityProvider;
import com.oursaviorgames.backend.auth.User;
import com.oursaviorgames.backend.gcs.CloudStorage;
import com.oursaviorgames.backend.gcs.GcsFile;
import com.oursaviorgames.backend.memcache.Cachable;
import com.oursaviorgames.backend.model.types.ValidatedUsername;
import com.oursaviorgames.backend.utils.DateUtils;

import java.util.Date;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

/**
 * User profile entity.
 */
@Cache
@Entity
public class UserProfile implements Cachable {

    /**
     * Indexed Entity field names.
     */
    public static final String F_Username               = "EF_Username";
    public static final String F_IdentityProvider       = "EF_IdentityProvider";
    public static final String F_IdentityProviderUserId = "EF_IdentityProviderUserId";

    /* Entity fields */
    @Id
    private Long   EF_Id;
    @Index
    private String EF_Username;
    @Index
    private String EF_IdentityProvider;
    @Index
    private String EF_IdentityProviderUserId;
    private Date   EF_Created;
    private String EF_DeviceId;
    private String EF_Name;
    private Gender EF_Gender;
    private GcsFile EF_ProfileImage;

    /**
     * Required default constructor for Objectify.
     */
    @SuppressWarnings("unused")
    private UserProfile() {
    }

    /**
     * Constructor.
     * @param username cannot be null.
     * @param identityProviderUserId cannot be null.
     * @param authority cannot be null.
     * @param gender user's gender. cannot be null.
     * @param name User's name. cannot be null
     * @param deviceId User's device registration id for push notifications. cannot be null.
     */
    public UserProfile(ValidatedUsername username, String identityProviderUserId,
                       IdentityProvider authority, Gender gender, String name, String deviceId) {
        EF_Id = null;
        EF_Username = username.getValue();
        EF_IdentityProviderUserId = checkNotNull(identityProviderUserId, "AuthorityUserId cannot be null");
        EF_IdentityProvider = checkNotNull(authority, "Authority cannot be null").getDomain();
        EF_Gender = checkNotNull(gender, "Gender cannot be null");
        EF_Name = checkNotNull(name, "User's name cannot be null");
        EF_DeviceId = checkNotNull(deviceId, "Device id cannot be null");
        EF_Created = DateUtils.getDate();
    }

    public Long getId() {
        return EF_Id;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key<UserProfile> getKey() {
        return Key.create(UserProfile.class, EF_Id);
    }

    /**
     * @return Returns user's internal username.
     */
    public String getUsername() {
        return EF_Username;
    }

    /**
     * Sets new username.
     */
    public void setUsername(ValidatedUsername username) {
        EF_Username = username.getValue();
    }

    /**
     * @return Return user's identity provider.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public IdentityProvider getIdentityProvider() {
        return IdentityProvider.parseDomain(EF_IdentityProvider);
    }

    public String getIdentityProviderDomain() {
        return EF_IdentityProvider;
    }

    /**
     * @return Returns user's identity provider's user id.
     */
    public String getAuthorityUserId() {
        return EF_IdentityProviderUserId;
    }

    public Date getDateCreated() {
        return EF_Created;
    }

    /**
     * @return Returns user's gender.
     */
    public Gender getGender() {
        return EF_Gender;
    }

    /**
     * @return Returns user's name.
     */
    public String getName() {
        return EF_Name;
    }

    /**
     * Returns thumbnail url of user's profile image file, or null if none has been set.
     */
    public String getProfileThumbUrl() {
        if (EF_ProfileImage != null) {
            GcsFilename thumb = CloudStorage.getThumbnailProfilePicFile(EF_ProfileImage.createGcsFileName());
            return CloudStorage.getObjectUrl(thumb);
        }
        return null;
    }

    /**
     * Returns url of the user's profile image file, or null if none has been set.
     */
    public String getProfileImageUrl() {
        if (EF_ProfileImage != null) {
            return CloudStorage.getObjectUrl(EF_ProfileImage.createGcsFileName());
        }
        return null;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public GcsFile getProfileImageFile() {
        return EF_ProfileImage;
    }

    /**
     * Sets user's profile image file.
     * <p>
     * Make sure profile image is already successfully stored before setting this value.
     *
     * @param profileImageFile Already stored profile image file.
     */
    public void setProfileImageFile(GcsFile profileImageFile){
        EF_ProfileImage = profileImageFile;
    }

    /**
     * @return Returns user's device registration id.
     */
    public String getDeviceId() {
        return EF_DeviceId;
    }

    /**
     * Creates UserProfile key from {@link com.oursaviorgames.backend.auth.User} object.
     * @param user Authenticated user object.
     * @return Datastore Key.
     */
    public static Key<UserProfile> createKey(User user) {
        return Key.create(UserProfile.class, user.getUserId());
    }

    /**
     * Creates UserProfile key from id.
     * @param id User's id.
     * @return Datastore Key.
     */
    public static Key<UserProfile> createKey(long id) {
        return Key.create(UserProfile.class, id);
    }

    /**
     * A person's gender.
     */
    public static enum Gender {
        OTHER,
        FEMALE,
        MALE
    }

}
