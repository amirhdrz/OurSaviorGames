package com.oursaviorgames.android.data.sql;

public class Field implements IField {

    /**
     * Table name.
     */
    public final String table;

    /**
     * Column name.
     */
    public final String column;

    /**
     * Fully qualified column name.
     */
    public final String name;

    public Field(String table, String column) {
        this.table = table;
        this.column = column;
        this.name = column + "." + column;
    }

    @Override
    public SortField asc() {
        return SortField.asc(this);
    }

    @Override
    public SortField desc() {
        return SortField.desc(this);
    }

}
