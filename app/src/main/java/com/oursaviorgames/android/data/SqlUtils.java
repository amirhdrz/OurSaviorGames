package com.oursaviorgames.android.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;

import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;

import com.oursaviorgames.android.data.sql.Alias;

/**
 * Convenience methods for creating SQL statements.
 */
public class SqlUtils {

    public static Field all(String table) {
        return DSL.field(table + ".*");
    }

    public static Field fieldFromAlias(Alias alias) {
        return DSL.fieldByName(alias.table, alias.column).as(alias.alias);
    }

    public static SortField sortField(String sortOrder) {
        String[] parts = sortOrder.split(" ");
        if (parts.length > 2) {
            throw new IllegalArgumentException("sortOrder (" + sortOrder + ") cannot be parsed");
        }
        if (parts[1].toLowerCase().equals("desc")) {
            return DSL.field(parts[0]).desc();
        } else if (parts[1].toLowerCase().equals("asc")) {
            return DSL.field(parts[0]).asc();
        } else {
            throw new IllegalArgumentException("Invalid sort direction (" + parts[1] + ")");
        }
    }


    /**
     * Convenience method for creating single-column index.
     * Index name has format: 'table_column'
     * @param table Table to create index for.
     * @param column Column to create index on.
     * @return raw sql statement.
     */
    public static String createIndex(String table, String column) {
        return String.format("CREATE INDEX %1$s_$2$s ON %1$s (%2$s)", table, column);

    }

    /**
     * Convenience method for creating single-column unique index.
     * Index name has format: 'table_column'
     * @param table Table to create index for.
     * @param column Column to create index on.
     * @return raw sql statement.
     */
    public static String createUniqueIndex(String table, String column) {
        return String.format("CREATE UNIQUE INDEX %1$s_$2$s ON %1$s (%2$s)", table, column);
    }

    /**
     * Convenience method for dropping an index created by {@link #createUniqueIndex(String, String)}
     * or {@link #createIndex(String, String)}.
     * Index name has format: 'table_name'
     */
    public static String dropIndex(String table, String column) {
        return String.format("DROP INDEX %1$s_%2$s", table, column);
    }

    /**
     * Creates simple selection clause for {@link android.content.ContentResolver} query.
     * @param column column to select on.
     * @param value value to match. Must have valid toString implementation.
     */
    public static String equal(String column, Object value) {
        return column + " = '" + String.valueOf(value) + "'";
    }

    /**
     * Creates simple selection clause for {@link android.content.ContentResolver} query.
     *
     * @param column column to select on.
     * @param value value to not equal to. Must have valid toString implementation.
     */
    public static String neq(String column, Object value) {
        return column + " != '" + String.valueOf(value) + "'";
    }

    /**
     * Creates greater than or equal to condition on column with value.
     * @param column column to select on.
     * @param value Must have valid toString implementation.
     * @return
     */
    public static String geq(String column, Object value) {
        return column + " >= '" + String.valueOf(value) + "'";
    }

   /**
     * Creates less than or equal to condition on column with value.
     * @param column column to select on.
     * @param value Must have valid toString implementation.
     * @return
     */
    public static String leq(String column, Object value) {
        return column + " <= '" + String.valueOf(value) + "'";
    }

    /**
     * Creates and clause for two sql selection parts for a {@link android.content.ContentResolver}
     * query.
     * @param parts conditions to AND together.
     */
    public static String and(String... parts) {
        return joinParts(parts, "and");
    }

    /**
     * Joins parts with ' or '.
     * Use for creating selection argument to use for {@link android.content.ContentResolver}.
     * @param parts conditions to OR together.
     * @return
     */
    public static String or(String... parts) {
        return joinParts(parts, "or");
    }

    private static String joinParts(String[] parts, String operator) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (String part : parts) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(' ');
                sb.append(operator);
                sb.append(' ');
            }
            sb.append('(');
            sb.append(part);
            sb.append(')');
        }
        return sb.toString();
    }

    /**
     * Create a Left outer join SQL statement.
     * Columns should not include their table name.
     * @param lT left table.
     * @param rT right table.
     * @param tT third table in outer join.
     * @param joinCol Common column name to join on.
     * @param sortOrder Column in leftTable to sort by.
     * @return
     */
    //TODO: this is a very specific implementation, however the naming implies it generalizes more broadly, which it doesn't.
    @Deprecated //use jooq instead.
    public static String createLeftOuterJoin(String lT, String rT, String tT,
                                             String rTIdAlias, String tTIdAlias,
                                             String joinCol, String sortOrder) {

        final String SQLStatement =
                "SELECT " + lT + ".*, " + rT + "._ID AS " + rTIdAlias + ((tT == null) ? "" : (", " + tT + "._ID AS " + tTIdAlias)) +
                " FROM " + lT +
                " LEFT JOIN " + rT + " ON " + lT + "." + joinCol + " = " + rT + "." + joinCol +
                ((tT == null) ? "" : (" LEFT JOIN " + tT + " ON " + lT + "." + joinCol + " = " + tT + "." + joinCol));

        if (sortOrder == null) {
            return SQLStatement;
        } else {
            return SQLStatement + " ORDER BY " + lT + "." + sortOrder;
        }
    }

    @Deprecated //use jooq instead.
    public static String createLeftOuterJoin(String lT, String rT, String rTIdAlias,
                                             String joinCol, String sortOrder) {

        return createLeftOuterJoin(lT, rT, null, rTIdAlias, null, joinCol, sortOrder);
    }

    /*
        ContentProvider helper functions
     */
    /**
     * Queries the given database table looking for row with given id.
     */
    public static Cursor executeQueryWithId(SQLiteDatabase db, String table, String[] projections, long id) {
        return db.query(
                table,
                projections,
                BaseColumns._ID + " = '" + id + "'",
                null,
                null,
                null,
                null
        );
    }

    /** Inserts row into the database or throws an exception */
    public static Uri insertRowOrThrow(SQLiteDatabase db, Uri contentUri, String table, ContentValues values) {
        long _id = db.insert(table, null, values);
        if (_id != -1) {
            return ContentUris.withAppendedId(contentUri, _id);
        } else {
            throw new SQLException("Failed to insert row into " + table +
                    " with values : " + values.toString());
        }
    }

    /** Updates a single row in the database or throws an exception */
    public static int updateSingleRowOrThrow(SQLiteDatabase db, String table, ContentValues values, long id) {
        int rowsUpdated = db.update(
                table,
                values,
                BaseColumns._ID + " = '" + id + "'",
                null
        );
        if (rowsUpdated != 1) {
            throw new SQLiteException("Failed to update row (" + id + ") in "  + table +
                    " with values: " + values.toString());
        }
        return rowsUpdated;
    }

    /**
     * Bulk insertion of values into table.
     * Opens a database transaction to do the inserts.
     * @return number of rows added.
     */
    public static int bulkInsertHelper(SQLiteDatabase db, String table, ContentValues[] values) {
        int rowsAdded = 0;
        db.beginTransaction();
        try {
            for (ContentValues value : values) {
                long _id = db.insert(table, null, value);
                if (_id != -1) {
                    rowsAdded++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return rowsAdded;
    }

}
