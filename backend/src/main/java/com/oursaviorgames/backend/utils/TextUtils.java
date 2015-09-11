package com.oursaviorgames.backend.utils;

/**
 * Text utility functions.
 */
public class TextUtils {

    /**
     * Returns empty String if string is null,
     * otherwise returns string.
     * @return Empty String or string.
     */
    public static String emptyStringIfNull(String string) {
        return (string == null)? "" : string;
    }

    /**
     * Returns true if a and b are equal, including if they are both null.
     * @param a first CharSequence to check
     * @param b second CharSequence to check
     * @return true if a and b are equal
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

}
