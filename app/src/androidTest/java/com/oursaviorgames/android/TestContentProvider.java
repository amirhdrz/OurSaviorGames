package com.oursaviorgames.android;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;
import com.google.api.client.util.DateTime;

import com.oursaviorgames.android.util.DateUtils;
import com.oursaviorgames.android.data.ExtraGamesHelper;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.GameDbHelper;
import com.oursaviorgames.android.data.GameContract.HotGameEntry;
import com.oursaviorgames.android.data.GameContract.NewGameEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameEntry;
import com.oursaviorgames.android.data.GameContract.PlayHistoryEntry;
import com.oursaviorgames.android.data.GameContract.ExtraGamesEntry;
import com.oursaviorgames.android.data.GameContract.SavedGameListEntry;
import com.oursaviorgames.android.data.UserDataContract.PlayTokensEntry;

import java.util.Date;
import java.util.Set;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * NOTES: all functions that start with 'test' are executed with the test suite.
 */
public class TestContentProvider extends AndroidTestCase {

    private static final String TAG = makeLogTag(TestContentProvider.class);

    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                HotGameEntry.CONTENT_URI, null, null
        );
        mContext.getContentResolver().delete(
                NewGameEntry.CONTENT_URI, null, null
        );
        mContext.getContentResolver().delete(
                SavedGameEntry.CONTENT_URI, null, null
        );
        mContext.getContentResolver().delete(
                PlayHistoryEntry.CONTENT_URI, null, null
        );
        mContext.getContentResolver().delete(
                PlayTokensEntry.CONTENT_URI, null, null
        );
        mContext.getContentResolver().delete(
                ExtraGamesEntry.CONTENT_URI, null, null
        );
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        deleteAllRecords();
    }

    /**
     * Tests {@link android.content.ContentResolver#getType(android.net.Uri)}
     * returns corrent value.
     */
    public void testGetType() {
        String type;
        ContentResolver r = getContext().getContentResolver();

        // play_history table
        type = r.getType(PlayHistoryEntry.CONTENT_URI);
        assertEquals(PlayHistoryEntry.CONTENT_TYPE, type);
        type = r.getType(PlayHistoryEntry.buildPlayHistoryUri(1l));
        assertEquals(PlayHistoryEntry.CONTENT_ITEM_TYPE, type);

        // Tests URIs to hot_games table
        type = r.getType(HotGameEntry.CONTENT_URI);
        assertEquals(HotGameEntry.CONTENT_TYPE, type);
        type = r.getType(HotGameEntry.buildHotGameUri(1l));
        assertEquals(HotGameEntry.CONTENT_ITEM_TYPE, type);

        // Tests URIs to new_games table
        type = r.getType(NewGameEntry.CONTENT_URI);
        assertEquals(NewGameEntry.CONTENT_TYPE, type);
        type = r.getType(NewGameEntry.buildNewGameUri(1l));
        assertEquals(NewGameEntry.CONTENT_ITEM_TYPE, type);

        // Tests URIs to saved_games table
        type = r.getType(SavedGameEntry.CONTENT_URI);
        assertEquals(SavedGameEntry.CONTENT_TYPE, type);
        type = r.getType(SavedGameEntry.buildSavedGameUri(1l));
        assertEquals(SavedGameEntry.CONTENT_ITEM_TYPE, type);

        // Tests URIs to play_tokens table
        type = r.getType(PlayTokensEntry.CONTENT_URI);
        assertEquals(PlayTokensEntry.CONTENT_TYPE, type);
        type = r.getType(PlayTokensEntry.buildPlayTokenUri(1l));
        assertEquals(PlayTokensEntry.CONTENT_ITEM_TYPE, type);

        // extra_games table.
        type = r.getType(ExtraGamesEntry.CONTENT_URI);
        assertEquals(ExtraGamesEntry.CONTENT_TYPE, type);
        type = r.getType(ContentUris.withAppendedId(ExtraGamesEntry.CONTENT_URI, 1l));
        assertEquals(ExtraGamesEntry.CONTENT_ITEM_TYPE, type);
    }

    /** We use {@link com.oursaviorgames.android.data.ExtraGamesHelper} exclusively to test
     *  extra_games table.
     */
    public void testExtraGamesTable() {
        // for extra caution we iterate over all of the keys.
        final String[] KEYS = new String[] {ExtraGamesHelper.RANDOM_GAME, ExtraGamesHelper.SPOTLIGHT_GAME};
        // testing...
        for (String key : KEYS) {
            // tests null is returned if the table is empty.
            ContentValues empty = ExtraGamesHelper.getExtraGame(mContext, key);
            assertNull(empty);
            // insert some values into the database.
            ContentValues v = TestData.createBaseGameTestValues();
            ExtraGamesHelper.putExtraGame(mContext, v, key);
            // retrieves the data and checks if it is as expected.
            ContentValues retrievedValues = ExtraGamesHelper.getExtraGame(mContext, key);
            validateContentValues(retrievedValues, v);
        }
    }

    public void testInsertReadProvider() {
        GameDbHelper dbHelper = new GameDbHelper(mContext);

        // Tests hot_games table
        ContentValues hotGameTestValues = TestData.createBaseGameTestValues();
        helperTestInsertReadProvider(mContext, HotGameEntry.CONTENT_URI, hotGameTestValues);

        // Tests new_games table
        ContentValues newGameTestValues = TestData.createBaseGameTestValues();
        helperTestInsertReadProvider(mContext, NewGameEntry.CONTENT_URI, newGameTestValues);

        // Tests saved_games table
        ContentValues savedGameTestValues = TestData.createBaseGameTestValues();
        savedGameTestValues.put(GameContract.SavedGameEntry.COLUMN_DATE_SAVED,
                DateUtils.getCurrentZuluTime());
        helperTestInsertReadProvider(mContext, SavedGameEntry.CONTENT_URI, savedGameTestValues);

        // tests play_history table
        ContentValues playHistoryValues = TestData.createBaseGameTestValues();
        playHistoryValues.put(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, 2);
        playHistoryValues.put(PlayHistoryEntry.COLUMN_PLAY_DURATION, 12313123);
        playHistoryValues.put(PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());
        helperTestInsertReadProvider(mContext, PlayHistoryEntry.CONTENT_URI, playHistoryValues);

        // Tests play_tokens table
        ContentValues tokenTestValues = TestData.createPlayToken();
        helperTestInsertReadProvider(mContext, PlayTokensEntry.CONTENT_URI, tokenTestValues);

        deleteAllRecords();
        dbHelper.close();
    }

    /** Test updating play_history table. */
    public void testUpdatePlayHistory() {
        ContentValues v = TestData.createBaseGameTestValues();
        v.put(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, 111);
        v.put(PlayHistoryEntry.COLUMN_PLAY_DURATION, 123123);
        v.put(PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());

        // Update necessary columns.
        // In reality some of these values should only be incremented.
        ContentValues v2 = new ContentValues();
        v2.put(PlayHistoryEntry.COLUMN_USER_PLAY_COUNT, 3);
        v2.put(PlayHistoryEntry.COLUMN_LAST_PLAYED, DateUtils.getCurrentZuluTime());
        v2.put(PlayHistoryEntry.COLUMN_PLAY_DURATION, 312313);

        helperInsertUpdateRow(mContext, PlayHistoryEntry.CONTENT_URI, v, v2);
    }

    /** Tests updating saved_games table. */
    public void testUpdateSavedGame() {
        // Create a new map of values, where column names are the keys
        ContentValues v = TestData.createBaseGameTestValues();
        v.put(SavedGameEntry.COLUMN_DATE_SAVED, DateUtils.getCurrentZuluTime());

        // Update some columns
        ContentValues v2 = new ContentValues();
        v2.put(SavedGameEntry.COLUMN_PLAYTIME, 2398472983479l);
        v2.put(SavedGameEntry.COLUMN_USER_PLAY_COUNT, 31231);
        v2.put(SavedGameEntry.COLUMN_LAST_PLAY_DATE,
                DateUtils.getTimestampString(new DateTime(new Date())));
        v2.put(SavedGameEntry.COLUMN_DATE_SAVED,
                DateUtils.getCurrentZuluTime());

        // test insert update values.
        helperInsertUpdateRow(mContext, SavedGameEntry.CONTENT_URI, v, v2);
    }

    /** Test left outer join between 'hot_games', 'saved_games' and 'play_history' */
    public void testGameListEntry() {
        //TODO: test with multiple rows
        //TODO: test with null values in each table.

        final String gameId = "1234";
        // insert data into hot_games table.
        ContentValues hotV = TestData.createBaseGameTestValues(gameId);
        helperTestInsertReadProvider(mContext, HotGameEntry.CONTENT_URI, hotV);
        // insert data into saved_games.
        ContentValues savedV = TestData.createSavedGameValues(gameId);
        long savedRowId = helperTestInsertReadProvider(mContext, SavedGameEntry.CONTENT_URI, savedV);
        // insert data into play_history table.
        ContentValues playV = TestData.createPlayHistoryTestValues(gameId);
        long playHistoryRowId = helperTestInsertReadProvider(mContext, PlayHistoryEntry.CONTENT_URI, playV);

        // Test
        Cursor data = mContext.getContentResolver().query(
                GameContract.HotGameListEntry.CONTENT_URI, null, null, null, HotGameEntry.COLUMN_HOT_SCORE);
        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.moveToFirst());
        ContentValues result = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(data, result);

        ContentValues expected = new ContentValues(hotV);
        expected.put(GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS, savedRowId);
        expected.put(GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS, playHistoryRowId);

        validateContentValues(result, expected);
    }

    /** Same as {@link #testGameListEntry()} with no saved game match */
    public void testGameListEntryNullSavedGame() {
        final String gameId = "433";
        // NewGameEntry
        ContentValues newV = TestData.createBaseGameTestValues(gameId);
        helperTestInsertReadProvider(mContext, NewGameEntry.CONTENT_URI, newV);

        // PlayHistoryEntry
        ContentValues playV = TestData.createPlayHistoryTestValues(gameId);
        long playHistoryRowId = helperTestInsertReadProvider(mContext, PlayHistoryEntry.CONTENT_URI, playV);

        // Expected values
        ContentValues expected = new ContentValues(newV);
        expected.put(GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS, (String) null);
        expected.put(GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS, playHistoryRowId);

        // Test
        Cursor data = mContext.getContentResolver().query(
                GameContract.NewGameListEntry.CONTENT_URI,
                null,
                null,
                null,
                NewGameEntry.COLUMN_DATE_PUBLISHED + " DESC"
        );

        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.moveToFirst());

        ContentValues result = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(data, result);
        validateContentValues(result, expected);
        data.close();
    }

    /** Same as {@link #testGameListEntry()} but with no play history. */
    public void testGameListEntryNullPlayHistory() {
        final String gameId = "3413123ff";

        // HotGameEntry
        ContentValues hotV = TestData.createBaseGameTestValues(gameId);
        helperTestInsertReadProvider(mContext, HotGameEntry.CONTENT_URI, hotV);

        // SavedGameEntry
        ContentValues savedV = TestData.createSavedGameValues(gameId);
        long savedRowId = helperTestInsertReadProvider(mContext, SavedGameEntry.CONTENT_URI, savedV);

        // Expected values
        ContentValues expected = new ContentValues();
        expected.put(GameContract.GameListColumns.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS, (String) null);
        expected.put(GameContract.GameListColumns.COLUMN_SAVED_TABLE_ID_ALIAS, savedRowId);

        // Test
        Cursor data = mContext.getContentResolver().query(
                GameContract.HotGameListEntry.CONTENT_URI,
                null,
                null,
                null,
                HotGameEntry.COLUMN_HOT_SCORE + " ASC"
        );

        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.moveToFirst());
        ContentValues result = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(data, result);

        validateContentValues(result, expected);

        data.close();
    }

    /** Tests {@link SavedGameListEntry}. */
    public void testSavedGameListEntry() {
        final String gameId = "234234";

        // SavedGameEntry
        ContentValues savedV = TestData.createSavedGameValues(gameId);
        helperTestInsertReadProvider(mContext, SavedGameEntry.CONTENT_URI, savedV);

        // PlayHistoryEntry
        ContentValues playV = TestData.createPlayHistoryTestValues(gameId);
        long playHistoryRowId = helperTestInsertReadProvider(mContext, PlayHistoryEntry.CONTENT_URI, playV);

        // Expected Values
        ContentValues expected = new ContentValues(savedV);
        expected.put(SavedGameListEntry.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS, playHistoryRowId);

        // Test
        Cursor data = mContext.getContentResolver().query(
                SavedGameListEntry.CONTENT_URI,
                null,
                null,
                null,
                SavedGameEntry.COLUMN_LAST_PLAY_DATE + " DESC"
        );

        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.moveToFirst());
        ContentValues result = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(data, result);

        validateContentValues(result, expected);

        data.close();
    }

    /** Tests {@link SavedGameListEntry} but with null {@link PlayHistoryEntry}. */
    public void testSavedGameListEntryNullPlayHistory() {
        final String gameId = "462642";

        // SavedGameEntry
        ContentValues savedV = TestData.createSavedGameValues(gameId);
        helperTestInsertReadProvider(mContext, SavedGameEntry.CONTENT_URI, savedV);

        // Expected Values
        ContentValues expected = new ContentValues(savedV);
        expected.put(SavedGameListEntry.COLUMN_PLAY_HISTORY_TABLE_ID_ALIAS, (String) null);

        // Test
        Cursor data = mContext.getContentResolver().query(
                SavedGameListEntry.CONTENT_URI,
                null,
                null,
                null,
                SavedGameEntry.COLUMN_LAST_PLAY_DATE + " DESC"
        );

        assertNotNull(data);
        assertTrue(data.getCount() > 0);
        assertTrue(data.moveToFirst());
        ContentValues result = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(data, result);

        validateContentValues(result, expected);

        data.close();
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    /*
        -----------------
        Helper functions
        -----------------
     */
    /**
     * Inserts row into table pointed to by {@code contentUri},
     * and checks if row has been inserted successfully or not.
     * @return rowId where the new record has been inserted.
     */
    static Uri helperInsertRow(Context context, Uri contentUri, ContentValues v) {
        Uri savedUri = context.getContentResolver()
                .insert(contentUri, v);
        assertTrue(ContentUris.parseId(savedUri) != -1);
        return savedUri;
    }

    /** Test helper: inserts row into db, updated it, and re-queries db
     *  to check if it's data is equal to {@code finalValues}. */
    static void helperInsertUpdateRow(Context context, Uri contentUri,
                                      ContentValues initialValues, ContentValues updatedValues) {
        // insert row first.
        Uri newRow = helperInsertRow(context, contentUri, initialValues);

        // update row
        int rowsUpdated = context.getContentResolver().update(
                newRow, updatedValues, null, null
        );
        // checks if newRow is updated successfully.
        assertEquals(rowsUpdated, 1);
        // read the row back from the database and checks if its equal to finalValues.
        Cursor result = context.getContentResolver().query(newRow, null, null, null, null);
        // build final expected values by adding updatedValues to initialValues.
        ContentValues finalValues = new ContentValues(initialValues);
        finalValues.putAll(updatedValues);
        // Test if result is as expected.
        validateCursor(result, finalValues);
    }

    //TODO: the functions below can just be imported from TestDb class.
    static long helperTestInsertReadProvider(Context context, Uri contentUri, ContentValues testValues) {
        Uri returnedUri = context.getContentResolver().insert(contentUri, testValues);
        long rowId = ContentUris.parseId(returnedUri);

        // Verify we got the row back
        assertTrue(rowId != -1);
        Log.d(TAG, "New row id: " + rowId);

        // Pulls out data to verify it has been inserted.
        Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);

        // Validate cursor
        validateCursor(cursor, testValues);
        return rowId;
    }

    /**
     * Validates first row of cursor with expectedValues.
     * Closes cursor before returning.
     * @param valueCursor Cursor to test.
     * @param expectedValues Expected values.
     */
    public static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        // If possible, move to the first row of the query results.
        assertTrue(valueCursor.moveToFirst());

        // get the content values out of the cursor at the current position
        ContentValues resultValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(valueCursor, resultValues);

        // make sure the values match the ones we put in
        validateContentValues(resultValues, expectedValues);
        valueCursor.close();
    }

    /**
     * Validates two ContentValues objects are equal.
     * For keys with null values, checks if both maps contain null.
     * @param output Values to test.
     * @param input Expected values.
     */
    public static void validateContentValues(ContentValues output, ContentValues input) {
        Set<String> inputKeys = input.keySet();
        for (String key : inputKeys) {
            assertTrue(output.containsKey(key));
            final String outValue = output.getAsString(key);
            final String inValue = input.getAsString(key);
            if (outValue == null) {
                assertNull(inValue);
            } else {
                assertTrue(output.getAsString(key).equals(input.getAsString(key)));
            }
        }
    }

}
