package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.oursaviorgames.android.util.UiUtils;

/**
 * A RelativeLayout with a height to width aspect ratio of 4:3
 * if the height of the display is less than 840dp, otherwise
 * a ration of 1:1 is used.
 * <p>
 * This View ignores 'layout_height' property and sets the height based
 * on the width.
 */
public class SmartRatioRelativeLayout extends RelativeLayout {

    private static final float THRESHOLD_1_1_DIPS = 840;

    private final float ratio;

    public SmartRatioRelativeLayout(Context context) {
        this(context, null);
    }

    public SmartRatioRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartRatioRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int screenHeight = size.y;
        if (screenHeight >= UiUtils.convertDipToPx(context, THRESHOLD_1_1_DIPS)) {
            ratio = 1.f;
        } else {
            ratio = 4.f / 3.f;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int w = MeasureSpec.getSize(widthMeasureSpec);
        final float height = w * ratio;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}