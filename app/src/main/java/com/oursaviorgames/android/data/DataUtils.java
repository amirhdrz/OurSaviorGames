package com.oursaviorgames.android.data;

import android.content.ContentValues;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DataUtils {

    /**
     * Returns COLUMN_ fields from contract obj.
     * @param contract Database contract class.
     */
    public static List<String> getColumns(Class contract) {
        List<String> columns = new ArrayList<>();
        Field[] allFields = contract.getFields();
        for (Field f : allFields) {
            final int m = f.getModifiers();
            if (Modifier.isPublic(m)
                    && Modifier.isStatic(m)
                    && Modifier.isFinal(m)
                    && f.getName().startsWith("COLUMN_")) {
                try {
                    columns.add((String) f.get(contract));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return columns;
    }

    public static void copyContentValues(ContentValues src, ContentValues dest, List<String> keys) {
        for (String field : keys) {
            Object value = src.get(field);
            if (value == null) {
                dest.putNull(field);
            } else if (value instanceof String) {
                dest.put(field, (String) value);
            } else if (value instanceof Integer) {
                dest.put(field, (Integer) value);
            } else if (value instanceof Boolean) {
                dest.put(field, (Boolean) value);
            } else if (value instanceof Long) {
                dest.put(field, (Long) value);
            } else if (value instanceof Float) {
                dest.put(field, (Float) value);
            } else {
                throw new RuntimeException("Value for field (" +  field + ") has unknown type");
            }
        }

    }

}
