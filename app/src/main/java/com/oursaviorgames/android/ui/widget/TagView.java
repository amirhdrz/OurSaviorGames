package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.oursaviorgames.android.util.UiUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class TagView extends TrapezoidView {

    private static final String TAG = makeLogTag(TagView.class);

    private static final float TAG_MARGIN_LEFT_DIPS = 4.f;

    private Drawable mDrawable;
    private float mTagMarginLeft;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTagMarginLeft = UiUtils.convertDipToPx(context, TAG_MARGIN_LEFT_DIPS);
    }

    public void setDrawable(Drawable drawable) {
        if (drawable != mDrawable) {
            mDrawable = drawable;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mDrawable != null) {
            LOGD(TAG, "drawable bounds: " + mDrawable.getBounds());
            final int intrinsicHeight = mDrawable.getIntrinsicHeight();
            final int intrinsicWidth = mDrawable.getIntrinsicWidth();
            LOGD(TAG, String.format("intrinsic bounds: %d, %d", intrinsicHeight, intrinsicWidth));
            if (intrinsicHeight == -1 || intrinsicWidth == -1) {
                LOGD(TAG, "bounds set");
                mDrawable.setBounds(0, 0, h, h);
            } else {
                mDrawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable != null) {
            canvas.save();
            canvas.translate(mTagMarginLeft, 0.f);
            mDrawable.draw(canvas);
            canvas.restore();
        }
    }
}
