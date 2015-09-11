package com.oursaviorgames.backend;

import com.google.api.server.spi.Constant;

/**
 * This class contains all the important pieces of data like API keys.
 * The content of this class should always be kept secret.
 * Contains the client IDs and scopes for allowed clients consuming your API.
 */
public final class Constants {

    public static final String FACEBOOK_APP_ID           = "INSERT_KEY";
    public static final String FACEBOOK_APP_SECRET       = "INSERT_KEY";
    public static final String FACEBOOK_APP_ACCESS_TOKEN = FACEBOOK_APP_ID + "|" + FACEBOOK_APP_SECRET;

    public static final String WEB_CLIENT_ID          = "INSERT_KEY";
    public static final String ANDROID_CLIENT_ID      = "INSERT_KEY";
    public static final String IOS_CLIENT_ID          = "replace this with iOS client ID";
    public static final String ANDROID_AUDIENCE       = WEB_CLIENT_ID;
    public static final String API_EXPLORER_CLIENT_ID = Constant.API_EXPLORER_CLIENT_ID;

    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";

}
