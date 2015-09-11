package com.oursaviorgames.android.ui.drawable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.LinearInterpolator;

import java.util.HashMap;
import java.util.Map;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class PixelationDrawable extends Drawable implements Animatable {

    private static final String TAG = makeLogTag(PixelationDrawable.class);

    private final Context mContext;
    private final Bitmap mBitmap;
    private final Paint mPaint;
    private final Matrix mMatrix;
    private final ObjectAnimator mObjectAnimator;

    //TODO:  we're not animating in reverse. just store previously scaled bitmap.
    private final Map<Integer, Bitmap> mScaledBitmaps;

    private int mScalingFactor;
    private boolean mAnimating;

    /**
     * @param context Application context.
     * @param bitmap Bitmap to pixelate.
     * @param animationDuration Animation duration in milliseconds.
     * @param degree degree of pixelation.
     */
    public PixelationDrawable(Context context, Bitmap bitmap, long animationDuration, int degree) {
        mContext = context;
        mBitmap = bitmap;
        mScaledBitmaps = new HashMap<>(degree, 1.f);

        mPaint = new Paint();
        mPaint.setAntiAlias(false);
        mPaint.setFilterBitmap(false);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mMatrix = new Matrix();

        Property<PixelationDrawable, Integer> mPixelationProperty = new Property<PixelationDrawable, Integer>(Integer.class, "pixelationFactor") {
            @Override
            public Integer get(PixelationDrawable object) {
                return object.getScalingFactor();
            }

            @Override
            public void set(PixelationDrawable object, Integer value) {
                object.setScalingFactor(1 << value);
            }
        };

        mObjectAnimator = ObjectAnimator.ofInt(this, mPixelationProperty, 1, degree);
        mObjectAnimator.setInterpolator(new LinearInterpolator());
        mObjectAnimator.setDuration(animationDuration);
    }

    public int getScalingFactor() {
        return mScalingFactor;
    }

    public void setScalingFactor (int factor) {
        mScalingFactor = factor;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        LOGD(TAG, "draw:: pixelation factor: " + mScalingFactor);
        // Retrieve scaled bitmap with current scaling factor.
        Bitmap bitmap = mScaledBitmaps.get(mScalingFactor);

        // If no such bitmap has been created, gets the next available
        // higher quality bitmap and scales it and
        // puts the new scaled bitmap into the HashMap.
        if (bitmap == null) {
            LOGD(TAG, "...draw:: creating new bitmap");
            Bitmap src = mScaledBitmaps.get(mScalingFactor / 2);
            if (src == null) {
                LOGD(TAG, "...draw::  using best next option");
                src = mBitmap;
            }
            Rect bounds = getBounds();
            bitmap = Bitmap.createScaledBitmap(src,
                    (bounds.width() + mScalingFactor - 1) / mScalingFactor, // round up during scaling.
                    (bounds.height() + mScalingFactor - 1) / mScalingFactor,
                    false);

            mScaledBitmaps.put(mScalingFactor, bitmap);
        } else {
            LOGD(TAG, "...draw:: bitmap already created");
        }
        mMatrix.reset();
        mMatrix.preScale(mScalingFactor, mScalingFactor);
        canvas.drawBitmap(bitmap, mMatrix, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        // ignore
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // ignore
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public void start() {
        if (mAnimating) return;
        mAnimating = true;
        mObjectAnimator.start();
        invalidateSelf();
    }

    /** If the animation is already running,
     * it will stop itself and play backwards from the point reached when reverse was called.
     * If the animation is not currently running, then it will start from the end and
     * play backwards.
     */
    public void reverse() {
        mObjectAnimator.reverse();
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (!mAnimating) return;
        mAnimating = false;
        mObjectAnimator.cancel();
        invalidateSelf();
    }

    @Override
    public boolean isRunning() {
        return mAnimating;
    }
}
