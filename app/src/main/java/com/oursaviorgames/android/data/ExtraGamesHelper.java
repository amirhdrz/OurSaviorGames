package com.oursaviorgames.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.oursaviorgames.android.data.metastore.DateMetaKey;
import com.oursaviorgames.android.data.metastore.MetaStore;
import com.oursaviorgames.android.util.DateUtils;

/**
 * Helper for inserting and retrieving extra games.
 */
public class ExtraGamesHelper {

    /** Database row key for spotlight game */
    public static final String SPOTLIGHT_GAME = "spotlight";

    /** Database row key for mock last played game */
    public static final String MOCK_LAST_PLAYED_GAME = "mock_last_played";

    /** Meta key for last time spotlight game was updated */
    public static final DateMetaKey META_SPOTLIGHT_LAST_UPDATE =
            new DateMetaKey(GameContract.ExtraGamesEntry.CONTENT_URI, SPOTLIGHT_GAME, DateUtils.REALLY_OLD_DATE);

    /**
     * Resets the flag of last update time
     * Note that this function just resets the flags,
     * and does not update the extra games.
     */
    public static void resetLastUpdateTime(Context context, String key) {
        throwIfKeyNotValid(key);
        MetaStore store = MetaStore.getMetaStore(context);
        if (SPOTLIGHT_GAME.equals(key)) {
            store.putMeta(META_SPOTLIGHT_LAST_UPDATE, null);
        } else {
            throw new IllegalArgumentException("Key is not valid: " + key);
        }
    }

    /**
     * Blocking function.
     * Performs database query retrieving extra game if any is stored.
     * @param context
     * @return ContentValues with keys from {@link com.oursaviorgames.android.data.BaseGameColumns},
     *          or null if no random game has been stored.
     */
    public static ContentValues getExtraGame(Context context, String key) {
        // Validates key.
        throwIfKeyNotValid(key);

        Cursor cursor = context.getContentResolver().query(
                GameContract.ExtraGamesEntry.CONTENT_URI,
                null,
                GameContract.ExtraGamesEntry.COLUMN_KEY + " = '" + key + "'",
                null,
                null
        );
        if (cursor != null) {
            ContentValues values = null;
            if (cursor.moveToFirst()) {
                values = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, values);
            }
            cursor.close();
            return values;
        }
        return null;
    }

    /**
     * Blocking function.
     * Puts extra game into the database.
     * @param values Must contains columns from {@link com.oursaviorgames.android.data.BaseGameColumns}.
     */
    public static void putExtraGame(Context context, final ContentValues values, String key) {
        // Validates key.
        throwIfKeyNotValid(key);
        // Create a new content values with key set.
        ContentValues v = new ContentValues(values);
        v.put(GameContract.ExtraGamesEntry.COLUMN_KEY, key);
        // If games doesn't exist in database, insert it, otherwise update row.
        if (getExtraGame(context, key) == null) {
            // insert
            context.getContentResolver().insert(GameContract.ExtraGamesEntry.CONTENT_URI, v);
        } else {
            // update existing row.
            context.getContentResolver().update(
                    GameContract.ExtraGamesEntry.CONTENT_URI,
                    v,
                    GameContract.ExtraGamesEntry.COLUMN_KEY + " = '" + key + "'",
                    null
            );
        }
    }

    /**
     * Always use this function to detect if passed in key is valid or not.
     */
    private static void throwIfKeyNotValid(String key) {
        if (!SPOTLIGHT_GAME.equals(key)
                && !MOCK_LAST_PLAYED_GAME.equals(key)) {
            throw new IllegalArgumentException("Key must be one of the values defined in this class");
        }
    }

}
