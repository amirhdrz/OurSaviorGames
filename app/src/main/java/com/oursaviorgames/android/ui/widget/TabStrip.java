package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.oursaviorgames.android.R;

/**
 * Used by {@link TabLayout}.
 * Do not use this view independently.
 */
class TabStrip extends LinearLayout {

    private static final int  SELECTED_INDICATOR_THICKNESS_DIPS    = 8;
    private static final int  SELECTED_INDICATOR_COLOR_RES_ID      = R.color.primaryDark;

    private final int   mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;

    private int   mSelectedPosition;
    private float mSelectionOffset;

    TabStrip(Context context) {
        this(context, null);
    }

    TabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();
        mSelectedIndicatorPaint.setColor(getContext().getResources().getColor(SELECTED_INDICATOR_COLOR_RES_ID));
    }


    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {

                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            canvas.drawRect(left, height - mSelectedIndicatorThickness, right,
                    height, mSelectedIndicatorPaint);
        }
    }

}