package com.oursaviorgames.android;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.oursaviorgames.android.data.GameDbHelper;
import com.oursaviorgames.android.data.UserDataDbHelper;

/**
 * NOTES: all functions that start with 'test' are executed with the test suite.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateGameDb() throws Throwable {
        mContext.deleteDatabase(GameDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new GameDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testCreateUserDataDb() throws Throwable {
        mContext.deleteDatabase(UserDataDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new UserDataDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

}
