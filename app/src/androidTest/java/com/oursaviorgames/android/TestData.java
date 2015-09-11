package com.oursaviorgames.android;

import android.content.ContentValues;

import java.util.Random;

import com.oursaviorgames.android.data.BaseGameColumns;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.util.DateUtils;
import com.oursaviorgames.android.data.UserDataContract;

/**
 * Convenience methods for generating test data.
 */
public class TestData {

    /**
     * Creates content values with mock data for "game base columns".
     * Some values are randomly generated and may cause conflict if they are
     * required to be unique.
     * @return ContentValues with mock test data.
     */
    public static ContentValues createBaseGameTestValues(String gameId) {
        final Random rand = new Random();

        // Create a new map of values, where column names are the keys.
        ContentValues v = new ContentValues();
        v.put(BaseGameColumns.COLUMN_GAME_ID, gameId);
        v.put(BaseGameColumns.COLUMN_GAME_TITLE, "Minecraft");
        v.put(BaseGameColumns.COLUMN_GAME_DESCRIPTION, "gamedescription");
        v.put(BaseGameColumns.COLUMN_DEVELOPER_ID, "did32");
        v.put(BaseGameColumns.COLUMN_DEVELOPER_NAME, "Mojang");
        v.put(BaseGameColumns.COLUMN_GAME_VERSION, rand.nextInt());
        v.put(BaseGameColumns.COLUMN_DATE_PUBLISHED, DateUtils.getCurrentZuluTime());
        v.put(BaseGameColumns.COLUMN_HOT_SCORE, rand.nextInt());
        v.put(BaseGameColumns.COLUMN_PLAY_COUNT, rand.nextInt());
        return v;
    }

    /**
     * Creates content values with mock data for "game base columns".
     * Some values are randomly generated and may cause conflict if they are
     * required to be unique.
     * @return ContentValues with mock test data.
     */
    public static ContentValues createBaseGameTestValues() {
        final Random rand = new Random();
        return createBaseGameTestValues(String.valueOf(rand.nextInt()));
    }

    /**
     * Creates test values for SavedGameEntry
     * @param gameId
     * @return
     */
    public static ContentValues createSavedGameValues(String gameId) {
        ContentValues v = createBaseGameTestValues(gameId);
        v.put(GameContract.SavedGameEntry.COLUMN_PLAYTIME, 123456);
        v.put(GameContract.SavedGameEntry.COLUMN_USER_PLAY_COUNT, 13245);
        v.put(GameContract.SavedGameEntry.COLUMN_LAST_PLAY_DATE, DateUtils.getCurrentZuluTime());
        v.put(GameContract.SavedGameEntry.COLUMN_DATE_SAVED, DateUtils.getCurrentZuluTime());
        return v;
    }

    /**
     * Creates test values for PlayHistoryEntry.
     * @param gameId
     * @return
     */
    public static ContentValues createPlayHistoryTestValues(String gameId) {
        ContentValues v = createBaseGameTestValues(gameId);
        v.put(GameContract.PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, 13443);
        v.put(GameContract.PlayHistoryEntry.COLUMN_PLAY_DURATION, 2342342);
        v.put(GameContract.PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());
        return v;
    }

    /**
     * Creates content values with mock data for play token.
     * @return
     */
    public static ContentValues createPlayToken() {
        ContentValues testValues = new ContentValues();
        testValues.put(UserDataContract.PlayTokensEntry.COLUMN_PLAY_DURATION, 12341234);
        testValues.put(UserDataContract.PlayTokensEntry.COLUMN_GAME_ID, "21234");
        testValues.put(UserDataContract.PlayTokensEntry.COLUMN_TIME_STAMP, DateUtils.getCurrentZuluTime());

        return testValues;
    }

}
