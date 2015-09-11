package com.oursaviorgames.android.util;

import android.net.Uri;

/**
 * Utility functions for {@link Uri}.
 */
public class UriUtils {

    /**
     * Returns a new Uri without the appended id to uri if the uri ends like '/#'
     * <p>
     * If uri doesn't have an appended id, returns the uri object itself.
     */
    public static Uri removeAppendedId(Uri uri) {
        final String uriString = uri.toString();
        int lastDelimiterInd = uriString.lastIndexOf('/');

        // if last char is '/' return uri.
        if (lastDelimiterInd + 1== uriString.length()) {
            return uri;
        }

        String lastPart = uriString.substring(lastDelimiterInd + 1, uriString.length());
        // is the last part a number?
        try {
            //noinspection ResultOfMethodCallIgnored
            Long.valueOf(lastPart);
        } catch (NumberFormatException e) {
            // last part is not a number
            return uri;
        }

        // create new uri with appended id removed.
        return Uri.parse(uriString.substring(0, lastDelimiterInd));
    }

}
