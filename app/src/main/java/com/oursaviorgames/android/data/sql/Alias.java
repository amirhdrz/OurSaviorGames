package com.oursaviorgames.android.data.sql;

/**
 * Represents a column alias.
 */
//TODO: doesn't really make sense to separate alias from field
public class Alias extends Field {

    /**
     * Column alias.
     */
    public final String alias;

    public Alias(String table, String column) {
        super(table, column);
        this.alias = table + "_" + column;
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
