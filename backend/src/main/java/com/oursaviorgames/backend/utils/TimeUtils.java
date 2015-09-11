package com.oursaviorgames.backend.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    /**
     * Returns current time as {@link Date} object.
     * @return current Date.
     */
    public static Date getCurrentTime() {
        return Calendar.getInstance().getTime();
    }

    public static Date getDateFromUnixTime(long unixTime) {
        return new Date(unixTime * 1000l);
    }

    /**
     * Returns current unix time.
     * @return Unix time.
     */
    public static long getUnixTime() {
        return Calendar.getInstance().getTime().getTime() / 1000l;
    }

    /**
     * Compares two unix times.
     * If {@code unixTime1} is strictly greater than {@code unixTime2} + {@code delta},
     * returns true.
     * @param unixTime1
     * @param unixTime2
     * @param deltaInSec in seconds.
     * @return
     */
    public static boolean compareTime (long unixTime1, long unixTime2, long deltaInSec) {
        long diff = unixTime1 - (unixTime2 + deltaInSec);
        return (diff > 0l);
    }

    /**
     * If {@code unixTime} is strictly greater than (current time + delta)
     * returns true.
     * Same as {@link #compareTime(long, long, long)}, but with current time.
     * @param unixTime
     * @param deltaInSec in seconds.
     * @return
     */
    public static boolean compareToNow(long unixTime, long deltaInSec) {
        return compareTime(unixTime, getUnixTime(), deltaInSec);
    }

}
