package com.oursaviorgames.android.util;

import android.util.Log;

import com.oursaviorgames.android.BuildConfig;

/**
 * Use this class for logging data, DEBUG flag is set here.
 */
public class LogUtils {

    private static final String LOG_PREFIX         = "osg_";
    private static final int    LOG_PREFIX_LENGTH  = LOG_PREFIX.length();
    private static final int    MAX_LOG_TAG_LENGTH = 23;

    /**
     * Debug mode.
     */
    public static final boolean DEBUG = BuildConfig.DEBUG;

    public static String makeLogTag(String tag) {
        // truncates tag if it's more than allowed max log tag length.
        if (tag.length() + LOG_PREFIX_LENGTH > MAX_LOG_TAG_LENGTH) {
            return LOG_PREFIX + tag.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + tag;
    }

    /**
     * Don't use this when obfuscating class names.
     */
    public static String makeLogTag(Class clz) {
        return makeLogTag(clz.getSimpleName());
    }

    public static void LOGD(String tag, String msg) {
        if (DEBUG) Log.d(tag, msg);
    }

    public static void LOGW(String tag, String msg) {
        if (DEBUG) Log.w(tag, msg);
    }

    public static void LOGE(String tag, String msg) {
        if (DEBUG) Log.e(tag, msg);
    }

    public static void LOGI(String tag, String msg) {
        if (DEBUG) Log.i(tag, msg);
    }

    /**
     * Convenience function for calling toString on each array item for logging.
     */
    public static <T> String printArray(T[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean firstTime = true;
        for (T item : array) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(", ");
            }
            sb.append(item.toString());
        }
        sb.append('}');
        return sb.toString();
    }

}
