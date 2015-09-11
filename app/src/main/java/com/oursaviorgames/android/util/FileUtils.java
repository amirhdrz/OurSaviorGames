package com.oursaviorgames.android.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * DirService manages the top level directories in this app.
 * Directories could be cached or not.
 */
public class FileUtils {

    /**
     * Returns the specified directory. It is guaranteed to be created.
     * @param context Application context.
     * @param dir Directory to return.
     * @return
     */
    public static File getDir(Context context, DIR dir) {
        File fileDir;
        if (dir.isInCacheDir()) {
            fileDir = new File(context.getCacheDir(), dir.getDirName());
            //noinspection ResultOfMethodCallIgnored
            fileDir.mkdir();
        } else {
            fileDir = context.getDir(dir.getDirName(), Context.MODE_PRIVATE);
        }
        return fileDir;
    }

    /**
     * Returns File object containing the user profile picture.
     */
    public static File getProfilePictureFile(Context context) {
        return new File(getDir(context, DIR.USER), "profile-picture.png");
    }

    /**
     * Returns directory for cached games.
     * <p>
     * Directory
     */
    public static File getCachedGamesDir(Context context) throws IOException {
        final File cachedGamesDir = new File(getDir(context, DIR.GAMES), "cache");
        boolean exists = (cachedGamesDir.mkdir() || cachedGamesDir.isDirectory());
        if (!exists) {
            throw new IOException("Caches games directory IOException");
        }
        return cachedGamesDir;
    }

    /**
     * Returns directory for saved games.
     */
    public static File getSavedGamesDir(Context context) throws IOException {
        final File file = new File(getDir(context, DIR.GAMES), "saved");
        boolean exists = (file.mkdir() || file.isDirectory());
        if (!exists) {
            throw new IOException("Saved games directory IOException");
        }
        return file;
    }

    /**
     * Cleans entire cache directory.
     * @return True if successful, false otherwise.
     */
    public static boolean cleanCacheDir(Context context) {
        try {
            org.apache.commons.io.FileUtils.cleanDirectory(context.getCacheDir());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Cleans video cache directory.
     * And disk caches dependent on this directory must be closed before
     * making this call.
     * Blocks until cache is cleared.
     * @return True if successful, false otherwise.
     */
    public static boolean cleanVideoCache(Context context) {
        try {
            org.apache.commons.io.FileUtils.cleanDirectory(getDir(context, DIR.VIDEOS));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Creates {@link Uri} object from file.
     * Unlike {@link Uri#fromFile(java.io.File)}, if file is null,
     * null is returned.
     */
    public static Uri toUri(File file) {
        return (file != null)? Uri.fromFile(file) : null;
    }

    /**
     * Directories provided by the {@link FileUtils}.
     */
    public static enum DIR {
        VIDEOS      (true, "videos"),
        PROFILE_PICS(true, "profiles"),
        GAMES       (false, "games"),
        USER        (false, "user"),
        GAME_CACHE  (false, "game_cache"),
        TEMP        (false, "tmp");

        private boolean isInCacheDir;
        private String  dirName;

        /**
         * @param isInCacheDir Is this directory in the cached directory.
         * @param dirName The name of the directory file.
         */
        DIR(boolean isInCacheDir, String dirName) {
            this.isInCacheDir = isInCacheDir;
            this.dirName = dirName;
        }

        public boolean isInCacheDir() {
            return isInCacheDir;
        }

        public String getDirName() {
            return dirName;
        }

    }
}
