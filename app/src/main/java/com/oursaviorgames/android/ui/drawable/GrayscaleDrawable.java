package com.oursaviorgames.android.ui.drawable;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.LinearInterpolator;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class GrayscaleDrawable extends Drawable implements Animatable {

    private static final String TAG = makeLogTag(GrayscaleDrawable.class);

    private final ColorMatrix mColorMatrix;
    private final Paint mPaint;
    private final Bitmap mBitmap;
    private final ObjectAnimator mObjectAnimator;

    private float mSaturation;
    private boolean mAnimating;

    /**
     * @param bitmap Bitmap to grayscale.
     * @param animationDuration Animation duration in milliseconds.
     */
    public GrayscaleDrawable(Bitmap bitmap, long animationDuration) {
        mBitmap = bitmap;
        mColorMatrix = new ColorMatrix();
        mPaint = new Paint();

        Property<GrayscaleDrawable, Float> mPixelationProperty = new Property<GrayscaleDrawable, Float>(Float.class, "pixelationFactor") {
            @Override
            public Float get(GrayscaleDrawable object) {
                return object.getSaturation();
            }

            @Override
            public void set(GrayscaleDrawable object, Float value) {
                object.setSaturation(value);
            }
        };

        mObjectAnimator = ObjectAnimator.ofFloat(this, mPixelationProperty, 1.f, 0.f);
        mObjectAnimator.setInterpolator(new LinearInterpolator());
        mObjectAnimator.setDuration(animationDuration);
    }

    public float getSaturation() {
        return mSaturation;
    }

    public void setSaturation(float sat) {
        mSaturation = sat;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        mColorMatrix.setSaturation(mSaturation);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(mColorMatrix);
        mPaint.setColorFilter(cf);
        canvas.drawBitmap(mBitmap, getBounds().left, getBounds().top, mPaint);
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

