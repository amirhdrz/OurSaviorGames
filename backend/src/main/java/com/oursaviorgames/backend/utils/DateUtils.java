package com.oursaviorgames.backend.utils;

import java.util.Date;

public class DateUtils {

    /**
     * Returns a new Date object.
     */
    public static Date getDate() {
        return new Date();
    }

    /**
     * Returns epoch time.
     */
    public static long getTimeNow() {
        return getDate().getTime();
    }

}
