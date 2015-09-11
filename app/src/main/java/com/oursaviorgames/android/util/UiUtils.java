package com.oursaviorgames.android.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.TextView;

import static com.oursaviorgames.android.util.LogUtils.LOGD;

public class UiUtils {

    public static int convertDipToPx(Context context, float dips) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, context.getResources().getDisplayMetrics());
    }

    public static int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.4f;
        return Color.HSVToColor(hsv);
    }

}
