package com.oursaviorgames.android.data.sql;

public class SortField {

    private final String fieldName;
    private final Direction direction;

    private SortField(String fieldName, Direction direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public static SortField asc(Field field) {
        return new SortField(field.name, Direction.ASC);
    }

    public static SortField desc(Field field) {
        return new SortField(field.name, Direction.DESC);
    }

    public static SortField asc(Alias alias) {
        return new SortField(alias.alias, Direction.ASC);
    }

    public static SortField desc(Alias alias) {
        return new SortField(alias.alias, Direction.DESC);
    }

    public String getSQL() {
        return fieldName + " " + direction.name();
    }

    private static enum Direction {
        ASC, DESC
    }

}
