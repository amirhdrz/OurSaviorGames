package com.oursaviorgames.android.util;

import com.google.api.client.util.DateTime;

import java.util.Date;

/**
 * Some utility functions for the database.
 */
public class DateUtils {

    /**
     * A really old date.
     * Actual value is Wed Nov 12 10:06:19 EST 2014.
     */
    public static Date REALLY_OLD_DATE = new Date(1415804779000l);

    /**
     * Converts {@link com.google.api.client.util.DateTime} object into RFC 3339 timestamp.
     * @param date
     * @return
     */
    public static String getTimestampString(DateTime date) {
        return date.toStringRfc3339();
    }

    /**
     * Returns current time in Zulu time zone in RFC 3339 encoding.
     */
    public static String getCurrentZuluTime() {
        return new DateTime(System.currentTimeMillis(), 0).toStringRfc3339();
    }

    /**
     * Returns current time in local time zone in RFC 3339 encoding.
     */
    public static String getCurrentLocalTime() {
        return new DateTime(System.currentTimeMillis()).toStringRfc3339();
    }

    /**
     * Returns RFC 3339 date-time with delta milliseconds from current time.
     * @param delta Number of milliseconds to add (if +ve) or subtract (if -ve) from current time.
     * @return RFC 3339 encoded date-time.
     */
    public static String getShiftedTime(long delta) {
        final long time = System.currentTimeMillis() + delta;
        return new DateTime(time, 0).toStringRfc3339();
    }

    /**
     * Converts {@code dateText} from RFC 3339 timestamp to {@link Date} object.
     * @param dateText RFC 3339 encoded timestamp.
     */
    public static DateTime getDateTimeFromTimestamp(String dateText) {
        return DateTime.parseRfc3339(dateText);
    }

    /**
     * Same as {@link #getDateTimeFromTimestamp(String)} (String)}, but returns
     * time in unix format.
     * @param dateText RFC 3339 encoded timestamp.
     */
    public static long getUnixTimeFromTimestamp(String dateText) {
        return DateTime.parseRfc3339(dateText).getValue();
    }

    /**
     * Returns true if (current time - date > threshold).
     * @param threshold Threshold time in milliseconds.
     */
    public static boolean isThresholdPassed(Date date, long threshold) {
        Date currentTime = new Date();
        return ((currentTime.getTime() - date.getTime()) > threshold);
    }

    /**
     * Implementation provided by {@link android.text.format.DateUtils#getRelativeTimeSpanString(long)}.
     * @param time time to convert.
     * @return relative time from now, e.g. 2 hours ago.
     */
    public static CharSequence getRelativeTimeSpanString(long time) {
        return android.text.format.DateUtils.getRelativeTimeSpanString(time);
    }

}
