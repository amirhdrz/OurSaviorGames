package com.oursaviorgames.android.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.HashMap;

import static com.oursaviorgames.android.util.LogUtils.LOGD;

public class TypefaceUtils {

    // Android implementation ignores load factor.
    // Based on current usage, initial capacity of 8 table would never need to expand.
    private static HashMap<Integer, Typeface> cache = new HashMap<>(8);

    /**
     * Typeface constants.
     */
    public static final int ROBOTO_THIN    = 0;
    public static final int ROBOTO_LIGHT   = 1;
    public static final int ROBOTO_REGULAR = 2;
    public static final int ROBOTO_MEDIUM  = 3;
    public static final int ROBOTO_BOLD    = 4;
    public static final int ROBOTO_BLACK   = 5;
    public static final int OCR_A          = 6;
    public static final int OCR_B          = 7;

    public static Typeface getTypeface(Context context, int fontStyle) {
        Typeface typeface = cache.get(fontStyle);
        if (typeface == null) {
            final String fontPath;
            switch (fontStyle) {
                case ROBOTO_THIN:
                    fontPath = "fonts/Roboto-Thin.ttf";
                    break;
                case ROBOTO_LIGHT:
                    fontPath = "fonts/Roboto-Light.ttf";
                    break;
                case ROBOTO_REGULAR:
                    fontPath = "fonts/Roboto-Regular.ttf";
                    break;
                case ROBOTO_MEDIUM:
                    fontPath = "fonts/Roboto-Medium.ttf";
                    break;
                case ROBOTO_BOLD:
                    fontPath = "fonts/Roboto-Bold.ttf";
                    break;
                case ROBOTO_BLACK:
                    fontPath = "fonts/Roboto-Black.ttf";
                    break;
                case OCR_A:
                    fontPath = "fonts/OCR-A-Regular.ttf";
                    break;
                case OCR_B:
                    fontPath = "fonts/OCRB.ttf";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid fontStyle " + fontStyle);
            }
            // Creates typeface and adds it to cache.
            typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
            cache.put(fontStyle, typeface);
            LOGD("TypefaceUtils", "cache size:" + cache.size());
        }
        return typeface;
    }

    /**
     * Applies Typeface to view.
     * @param view
     * @param fontStyle
     */
    public static void applyTypeface(TextView view, int fontStyle) {
        view.setTypeface(getTypeface(view.getContext(), fontStyle));
    }
}
