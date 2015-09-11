package com.oursaviorgames.android.game;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for Games files IO operations.
 */
class Util {

    /**
     * Computes key from gameId.
     * @param gameId
     * @return
     */
    @Deprecated
    public static String getKeyFromGameId(String gameId) {
        return "g" + gameId;
    }

    /**
     * Extracts {@code zipFile} content into directory {@code dir}.
     * @param destDir Directory where files are extracted into,
     *                creates directory if it doesn't already exist.
     * @param zipFile Zip file.
     */
    public static boolean unpackZIP(File zipFile, File destDir) {
        // TODO: close all the streams in the finally block.
        try {
            //noinspection ResultOfMethodCallIgnored
            destDir.mkdir();
            if (!destDir.isDirectory()) {
                throw new IllegalArgumentException("destDir is not a directory.");
            }
            InputStream is = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));

            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null) {
                // If ZipEntry is a directory, creates it.
                if (ze.isDirectory()) {
                    File file = new File(destDir, ze.getName());
                    //noinspection ResultOfMethodCallIgnored
                    file.mkdirs();
                    continue;
                }
                // Extracts content of zip entry.
                FileOutputStream out = new FileOutputStream(
                        new File(destDir, ze.getName()));
                while ((count = zis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.close(); // TODO: close all the streams in the finally block.
                zis.closeEntry();
            }
            zis.close(); // TODO: close all the streams in the finally block.
        } catch (FileNotFoundException e) {
            Log.e("unzip", String.format("Zip file %s not found", zipFile.getName()));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Moves all files in {@code srcDir} to within {@code destDir},
     * and <i>deletes</i> {@code srcDir} and all of its content.
     * Preserves {@code srcDir} if moving fails, and deletes any stub files.
     * @param srcDir Source Directory.
     * @param destDir Destination Directory.
     * @return new directory of moved files or null if an error had occurred.
     */
    public static File moveDir(File srcDir, File destDir) {
        // New directory in destDir to copy files to.
        File newDir = new File(destDir, srcDir.getName());
        try {
            // Copies srcDir to newDir.
            FileUtils.copyDirectory(srcDir, newDir, true);
            // Deletes the source directory quietly.
            FileUtils.deleteQuietly(srcDir);
            return newDir;
        } catch (IOException copyException) {
            // Deletes stub newDir.
            FileUtils.deleteQuietly(newDir);
            return null;
        }
    }

    /**
     * Looks for a directory named {@code gameDir} in directory {@code dir}.
     * @param dir Directory to search in.
     * @param gameKey File name of the directory to look for.
     * @return game File directory or null if not found.
     */
    public static File findGameInDir(File dir, String gameKey) {
        String[] fileNames = dir.list();
        for(String fileName : fileNames) {
            if (fileName.equals(gameKey)) {
                return new File(dir, gameKey);
            }
        }
        return null;
    }

}
