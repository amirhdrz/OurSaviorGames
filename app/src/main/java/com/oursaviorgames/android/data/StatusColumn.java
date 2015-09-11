package com.oursaviorgames.android.data;

/**
 * Status column.
 * <p>
 * Database entries that are synced with backend
 * can have their sync status tracked by constants
 * defined in this interface.
 */
public interface StatusColumn {

    /**
     * Status column for a row.
     * <p>
     * Values of the column should be restricted to
     * of the constants defined in this interface.
     *
     * <P>Type: INTEGER (int) DEFAULT 0</P>
     */
    public static final String _STATUS = "_status";

    public static final int NONE = 0;

    public static final int SENT = 1;

    public static final int SUCCESS = 2;

    public static final int FAILED = 3;

}
