package com.oursaviorgames.android.ui.drawable;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import com.oursaviorgames.android.R;

/**
 * Color drawable that rotates between it's color each time it is called.
 */
public class PlaceHolderDrawable extends ColorDrawable {

    private static int sColors[];
    private static int sCurrentIndex;

    public PlaceHolderDrawable(Context context) {
        if (sColors == null) {
            int red = context.getResources().getColor(R.color.red);
            int blue = context.getResources().getColor(R.color.blue);
            int green = context.getResources().getColor(R.color.green);
            sColors = new int[] {red, blue, green};
            sCurrentIndex = 0;
        }

        int color = sColors[sCurrentIndex];
        sCurrentIndex = (sCurrentIndex == sColors.length - 1)? 0 : (sCurrentIndex + 1);
        setColor(color);
    }
}
