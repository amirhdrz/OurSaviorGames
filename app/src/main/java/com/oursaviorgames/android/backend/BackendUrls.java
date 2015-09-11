package com.oursaviorgames.android.backend;

import android.content.Context;
import android.net.Uri;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.Utils;

/**
 * Utility class for backend needs.
 */
public class BackendUrls {

    private static final String ENDPOINT_URL = "https://robotic-algebra-633.appspot.com";
    private static final String ROOT_STORAGE_URL = "https://storage.googleapis.com/robotic-algebra-633.appspot.com/";
    private static final String GAME_FOLDER      = ROOT_STORAGE_URL + "g/";
    private static final String VIDEO_FOLDER     = ROOT_STORAGE_URL + "v/";

    /**
     * Returns string url of video on the server.
     * @param gameId Game id on the server.
     */
    public static String getVideoDownloadUrl(String gameId) {
        return VIDEO_FOLDER + gameId + ".webm";
    }

    /**
     * Returns string url of game zip file on the server.
     * @param gameId Game id on the server.
     * @return
     */
    public static String getGameDownloadUrl(String gameId) {
        return GAME_FOLDER + gameId + ".zip";
    }

    /***
     * Returns profile picture upload url
     */
    public static String getProfileUploadUrl() {
        return ENDPOINT_URL + "/photos";
    }

    /**
     * Returns share link for gameId;
     */
    public static String getShareLink(Context context, String gameId) {
        return "http://" + context.getString(R.string.com_oursaviorgames)
                + context.getString(R.string.com_oursaviorgames_path_play)
                + "?g=" + Utils.encodeToBase64(gameId);
    }

}
