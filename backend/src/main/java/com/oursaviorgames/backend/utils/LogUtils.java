package com.oursaviorgames.backend.utils;

import java.util.logging.Logger;

/**
 * Utilities for logging data.
 */
public class LogUtils {

    private static final boolean DEBUG = true;

    public static String makeLogTag(Class cls) {
        return cls.getSimpleName();
    }

    private static Logger getLogger(Class cls) {
        return Logger.getLogger(makeLogTag(cls));
    }

    public static void LOGI(String tag, String logMsg) {
        Logger.getLogger(tag).info(logMsg);
    }

    public static void LOGW(String tag, String logMsg) {
        Logger.getLogger(tag).warning(logMsg);
    }

    public static void LOGE(String tag, String logMsg) {
        Logger.getLogger(tag).severe(logMsg);
    }

    public static void LOGD(String tag, String logMsg) {
        if (DEBUG) Logger.getLogger(tag).info(logMsg);
    }

    public static void DEBUG_LOGI(String tag, String logMsg) {
        if (DEBUG) Logger.getLogger(tag).info(logMsg);
    }

    public static void DEBUG_LOGW(String tag, String logMsg) {
        if (DEBUG) Logger.getLogger(tag).warning(logMsg);
    }

    public static void DEBUG_LOGE(String tag, String logMsg) {
        if (DEBUG) Logger.getLogger(tag).severe(logMsg);
    }

}
