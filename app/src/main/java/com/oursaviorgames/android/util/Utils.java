package com.oursaviorgames.android.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.BackendUrls;
import com.oursaviorgames.android.data.model.GameInfoModel;

import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Utilities class.
 */
public final class Utils {

    public static String getVersionName(Context context) {
        String versionName = null;
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                versionName = packageInfo.versionName + " BETA";
            } catch (PackageManager.NameNotFoundException e) {
                // ignore.
            }
        }
        return versionName;
    }

    /**
     * Decodes base64 string.
     * @param base64 Non-null base64 encoded String.
     * @throws IllegalArgumentException If input is not valid base64 String.
     */
    public static String decodeBase64String(String base64) throws IllegalArgumentException {
        checkNotNull(base64, "Null String");
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return new String(decodedBytes);
    }

    /**
     * Base64-encodes string.
     */
    public static String encodeToBase64(String string) {
        checkNotNull(string, "Null string");
        return Base64.encodeToString(string.getBytes(), Base64.DEFAULT);
    }

    /**
     * Starts a choose Activity to share game specified by model.
     */
    public static void startActivityShareGame(Context context, GameInfoModel model) {
        final String url = BackendUrls.getShareLink(context, model.getGameId());

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        shareIntent.setType("text/plain");

        // We always show the chooser.
        Intent.createChooser(shareIntent, "share ...");
        context.startActivity(shareIntent);
    }

    /**
     * Utility function to determine whether network is available or not.
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return (activeNetworkInfo != null) && activeNetworkInfo.isConnected();
    }

    /**
     * Reads all content of a file and returns it as a string.
     * @param file File to read.
     * @return
     * @throws IOException
     */
    public static String readFileFully(File file) throws IOException{
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = new byte[(int)file.length()];
        fis.read(bytes);
        fis.close();
        return new String(bytes);
    }

    /**
     * Returns resource identifiers in the resource array.
     * If one of the attributes is not defined or is not a resource, resource id at that index is 0.
     * @param context Context.
     * @param arrayResId Id of the array resource.
     * @return Array of resourceIds.
     */
    @Deprecated
    public static int[] getResourceIdsFromTypedArray(Context context, int arrayResId) {
        TypedArray array = context.getResources().obtainTypedArray(arrayResId);
        int[] resIds = new int[array.length()];

        for (int i = 0; i < array.length(); i++) {
            resIds[i] = array.getResourceId(i, 0);
        }
        array.recycle();
        return resIds;
    }

    /**
     * Generates a random 128-bit UUID for the current user.
     * TODO: is the installation file saved across android backups.
     */
    @Deprecated
    public static class Installation {
        private static String sID = null;
        private static final String INSTALLATION = "INSTALLATION";

        /**
         * Returns unique id for this user.
         * @param context Context of the application.
         * @return random 128-bit UUID.
         */
        public synchronized static String id(Context context) {
            if (sID == null) {
                File installation = new File(context.getFilesDir(), INSTALLATION);
                try {
                    if (!installation.exists())
                        writeInstallationFile(installation);
                    sID = readInstallationFile(installation);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return sID;
        }

        private static String readInstallationFile(File installation) throws IOException {
            RandomAccessFile f = new RandomAccessFile(installation, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();
            return new String(bytes);
        }

        private static void writeInstallationFile(File installation) throws IOException {
            FileOutputStream out = new FileOutputStream(installation);
            String id = UUID.randomUUID().toString();
            out.write(id.getBytes());
            out.close();
        }
    }
}
