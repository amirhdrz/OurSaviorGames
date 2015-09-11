package com.oursaviorgames.android.game;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Game manifest.
 * This manifest is compatible with CrossWalks manifest file.
 */
public class GameManifest {

    public static final String MANIFEST_FILE = "manifest.json";

    /** JSON Nodes **/
    public static final String NODE_START_URL = "start_url";
    public static final String NODE_OFFLINE = "offline";
    public static final String NODE_ORIENTATION = "orientation";

    /** Constants **/
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;

    private final String mStartUrl;
    private final boolean mOffline;
    private final int mOrientation;


    public GameManifest(String jsonStr) throws JSONException{
        //Reads the json file
        JSONObject jsonObj = new JSONObject(jsonStr);

        // NODE_START_URL
        mStartUrl = jsonObj.getString(NODE_START_URL);

        // NODE_OFFLINE
        mOffline = jsonObj.getBoolean(NODE_OFFLINE);

        // NODE_ORIENTATION
        String orientation = jsonObj.getString(NODE_ORIENTATION);
        if (TextUtils.equals(orientation, "portrait")) {
            mOrientation = ORIENTATION_PORTRAIT;
        } else if (TextUtils.equals(orientation, "landscape")) {
            mOrientation = ORIENTATION_LANDSCAPE;
        } else {
            throw new JSONException("Expected orientation to be 'landscape' or 'portrait'");
        }
    }

    /**
     * Returns relative path to start url.
     * @return
     */
    public String getStartUrl() {
        return mStartUrl;
    }

    /**
     * Returns if game is locally cached.
     * @return
     */
    public boolean isOffline() {
        return mOffline;
    }

    /**
     * Returns one of {@code ORIENTATION} constants.
     * @return
     */
    public int getOrientation() {
        return mOrientation;
    }
}
