package com.oursaviorgames.backend.gcs;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;

import com.oursaviorgames.backend.utils.DateUtils;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

/**
 * All GcsFilenames returned by this class have their ObjectName
 * appended by epoch time the call was made to one of these methods.
 * <p>
 *
 */
public class CloudStorage {

    private static final AppIdentityService IDENTITY_SERVICE = AppIdentityServiceFactory.getAppIdentityService();
    public static final String DEFAULT_BUCKET = IDENTITY_SERVICE.getDefaultGcsBucketName();
    public static final String GCS_ROOT_SECURE = "https://storage.googleapis.com/";

    /**
     * Games folder name in Default bucket.
     */
    public static final String GAME_FOLDER_NAME = "g";

    /**
     * Videos folder name in Default bucket.
     */
    public static final String VIDEO_FOLDER_NAME = "v";

    /**
     * Profile pictures folder name in Default bucket.
     */
    public static final String PROFILE_PICS_FOLDER_NAME = "p";

    /**
     * Thumbnail profile pictures folder name in Default bucket.
     */
    public static final String THUMBNAIL_PROFILE_PICS_FOLDER_NAME = "thumb";

    /**
     * Returns GcsFilename for given object.
     */
    public static GcsFilename generateProfileImageFile(long userId) {
        if (userId <= 0l) {
            throw new IllegalArgumentException("userId must be >= 1");
        }
        String objectName = Long.toHexString(userId) + Long.toHexString(DateUtils.getTimeNow());
        return new GcsFilename(DEFAULT_BUCKET, PROFILE_PICS_FOLDER_NAME + "/" + objectName);
    }

    /**
     * Creates thumbnail GcsFilename based on already created profile image GcsFilename
     * returned from a previous call to {@link #generateProfileImageFile(long)}.
     * @param profileImageFile Result of previous call {@link #generateProfileImageFile(long)}.
     * @return GcsFilename for storing user's profile image thumbnail.
     */
    public static GcsFilename getThumbnailProfilePicFile(GcsFilename profileImageFile) {
        checkNotNull(profileImageFile, "Null profileImageObjectName");
        final String objectName = profileImageFile.getObjectName();
        return new GcsFilename(DEFAULT_BUCKET, THUMBNAIL_PROFILE_PICS_FOLDER_NAME + "/" + objectName);
    }

    /**
     * Returns GcsFilename for given object, or null if objectName is invalid.
     */
    //TODO: need to start generating random file names that will be timestamped.
    public static GcsFilename getGameFile(String objectName) {
        if (validateObjectName(objectName)) {
            return new GcsFilename(DEFAULT_BUCKET, GAME_FOLDER_NAME + "/" + objectName);
        }
        return null;
    }

    /**
     * Returns GcsFilename for given object, or null if objectName is invalid.
     */
    public static GcsFilename getVideoFile(String objectName) {
        if (validateObjectName(objectName)) {
            return new GcsFilename(DEFAULT_BUCKET, VIDEO_FOLDER_NAME + "/" + objectName);
        }
        return null;
    }

    public static GcsFilename getGcsFilenameDefaultBucket(String objectName) {
        checkNotNull(objectName, "Null objectName");
        return new GcsFilename(DEFAULT_BUCKET, objectName);
    }

    /**
     * Returns url pointing to file.
     */
    public static String getObjectUrl(GcsFilename file) {
        return GCS_ROOT_SECURE + file.getBucketName() + "/" + file.getObjectName();
    }

    /**
     * Validates given object name.
     * @param objectName name to check.
     * @return True if validated, false otherwise.
     */
    private static boolean validateObjectName(String objectName) {
        if (objectName == null) return false;
        if (objectName.contains("/")) return false;
        return true;
    }

}
