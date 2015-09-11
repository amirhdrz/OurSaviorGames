package com.oursaviorgames.backend.gcs;

import com.google.appengine.tools.cloudstorage.GcsFilename;

import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Same as {@link com.google.appengine.tools.cloudstorage.GcsFilename} but with no-arg constructor
 * to be used with Objectify.
 * <p>
 * Note that this class is not immutable
 *
 */
public class GcsFile implements Serializable {

    private String bucketName;
    private String objectName;

    public GcsFile() {
        // required no-arg constructor.
    }

    public GcsFile(String bucketName, String objectName) {
        this.bucketName = checkNotNull(bucketName, "Null bucketName");
        this.objectName = checkNotNull(objectName, "Null objectName");
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public GcsFilename createGcsFileName() {
        return new GcsFilename(bucketName, objectName);
    }

    public static GcsFile createGcsFile(GcsFilename gcsFilename) {
        return new GcsFile(gcsFilename.getBucketName(), gcsFilename.getObjectName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + bucketName + ", " + objectName + ")";
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GcsFile other = (GcsFile) o;
        return Objects.equals(bucketName, other.bucketName)
                && Objects.equals(objectName, other.objectName);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(bucketName, objectName);
    }

}
