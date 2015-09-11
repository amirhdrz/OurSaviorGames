package com.oursaviorgames.android.ui.drawable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.Random;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.UiUtils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class DiceDrawable extends Drawable implements Animatable {

    private static final String TAG = makeLogTag(DiceDrawable.class);

    private static final float        INTRINSIC_SIZE_DIPS = 27.f;
    private static final int          DICE_COLOR_RES_ID   = R.color.white;
    private static final int          BG_COLOR_RES_ID = R.color.primary;
    private static final float        CORNER_RADIUS_DIPS  = 3.f;
    private static final float        DOT_RADIUS_DIPS     = 3.f;
    private static final float        DOT_PADDING_DIPS    = 3.f;
    private static final long         ANIMATION_DURATION  = 150; // in milliseconds.
    private static final Interpolator PATH_INTERPOLATOR   = new LinearInterpolator();

    private final float  mPadding;
    private final Random mRandom;
    private final Paint  mPaint;
    private final Paint  mHolePaint;
    private final float  mCornerRadius;
    private final float  mDotRadius;
    private final int    mIntrinsicSize;

    private ObjectAnimator mObjectAnimatorPath;
    private RectF          mBoundsF;
    private PointF         mDotsInitial;
    private PointF[]       mDotsFinal;
    private PointF[]       mDotsCurrent;
    private int            mDiceNumber = -1;
    private float          mCurrentPathFraction = 1.f; // range: [0,1].
    private boolean        mAnimating = false;

    private Property<DiceDrawable, Float> mPathFractionProperty =
            new Property<DiceDrawable, Float>(Float.class, "pathFraction") {
                @Override
                public Float get(DiceDrawable object) {
                    return object.getPathFraction();
                }

                @Override
                public void set(DiceDrawable object, Float value) {
                    object.setPathFraction(value);
                }
            };

    public DiceDrawable(Context context) {
        mRandom = new Random();
        mCornerRadius = UiUtils.convertDipToPx(context, CORNER_RADIUS_DIPS);
        mPadding = UiUtils.convertDipToPx(context, DOT_PADDING_DIPS);
        mDotRadius = UiUtils.convertDipToPx(context, DOT_RADIUS_DIPS);
        mIntrinsicSize = UiUtils.convertDipToPx(context, INTRINSIC_SIZE_DIPS);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(context.getResources().getColor(DICE_COLOR_RES_ID));

        mHolePaint = new Paint();
        mHolePaint.setAntiAlias(true);
        mHolePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mHolePaint.setColor(context.getResources().getColor(BG_COLOR_RES_ID));

        setupAnimations();
    }

    private void setupAnimations() {
        mObjectAnimatorPath = ObjectAnimator.ofFloat(this, mPathFractionProperty, 0.f, 1.f);
        mObjectAnimatorPath.setInterpolator(PATH_INTERPOLATOR);
        mObjectAnimatorPath.setDuration(ANIMATION_DURATION);
//        mObjectAnimatorPath.setRepeatMode();
        mObjectAnimatorPath.setRepeatCount(1);
    }

    /**
     * Each time called chooses a different random dice number between 2 and 6.
     */
    public void setRandomDiceNumber() {
        int nextNum = mRandom.nextInt(5) + 2; // random number between 2 and 6
        if (nextNum == mDiceNumber) {
            // shift right by 1
            nextNum = (nextNum + 1);
            if (nextNum == 7) {
                nextNum = 2;
            }
        }
        setDiceNumber(nextNum);
    }

    public void setDiceNumber(int diceNumber) {
        if (diceNumber < 1 || diceNumber > 6) {
            throw new IllegalArgumentException("diceNumber has to be in range [1,6]");
        }
        mDiceNumber = diceNumber;
        if (getBounds().width() != 0
                && getBounds().height() != 0) {
            // If bounds has already been set
            // Initialize dots.
            initializeDots();
        }
    }

    private void initializeDots() {
        if (mDiceNumber != -1) {
            mDotsCurrent = new PointF[mDiceNumber];
            for (int i = 0; i < mDiceNumber; i++) {
                mDotsCurrent[i] = new PointF();
            }
            mDotsInitial = getInitialDotPosition();
            mDotsFinal = getDotPositions();
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicSize;
    }

    @Override
    public void start() {
        if (mAnimating) return;
        setRandomDiceNumber();
        mAnimating = true;
        mObjectAnimatorPath.start();
        invalidateSelf();
    }

    @Override
    public void stop() {
        if (!mAnimating) return;
        mAnimating = false;
        mObjectAnimatorPath.cancel();
        invalidateSelf();
    }

    @Override
    public boolean isRunning() {
        return mAnimating;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mBoundsF = new RectF(bounds);
        if (mDotsFinal == null) {
            setRandomDiceNumber();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mBoundsF, mCornerRadius, mCornerRadius, mPaint);
        for (int i = 0; i < mDotsFinal.length; i++) {
            mDotsCurrent[i].x = ((mDotsFinal[i].x - mDotsInitial.x) * mCurrentPathFraction) + mDotsInitial.x;
            mDotsCurrent[i].y = ((mDotsFinal[i].y - mDotsInitial.y) * mCurrentPathFraction) + mDotsInitial.y;
            canvas.drawCircle(mDotsCurrent[i].x, mDotsCurrent[i].y, mDotRadius, mHolePaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // ignore.
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public float getPathFraction() {
        return mCurrentPathFraction;
    }

    public void setPathFraction(float currentPathFraction) {
        mCurrentPathFraction = currentPathFraction;
        invalidateSelf();
    }

    /**
     * Returns the rect that bounds the dots on the dice.
     */
    private RectF getDotsBoundRect() {
        RectF rect = new RectF(mBoundsF);
        if (rect.width() == 0 || rect.height() == 0) {
            throw new IllegalStateException("Drawable bounds has not been set");
        }
        // Have to adjust for the radius too.
        rect.inset(mPadding + mDotRadius, mPadding + mDotRadius);
        return rect;
    }

    /**
     * Returns the center dot position
     */
    private PointF getInitialDotPosition() {
        RectF rect = getDotsBoundRect();
        return new PointF(rect.centerX(), rect.centerY());
    }

    /**
     * Returns the position of all the dots for the current {@code mDiceNumber}.
     * @return Returned array is as large as {@code mDiceNumber}.
     */
    private PointF[] getDotPositions() {
        RectF bounds = getDotsBoundRect();

        PointF[] dots = new PointF[mDiceNumber];

        switch (mDiceNumber) {
            case 1:
                dots[0] = new PointF(bounds.centerX(), bounds.centerY());
                break;
            case 2:
                dots[0] = new PointF(bounds.left, bounds.top);
                dots[1] = new PointF(bounds.right, bounds.bottom);
                break;
            case 3:
                dots[0] = new PointF(bounds.centerX(), bounds.centerY());
                dots[1] = new PointF(bounds.right, bounds.top);
                dots[2] = new PointF(bounds.left, bounds.bottom);
                break;
            case 4:
                setFourCorners(dots, bounds);
                break;
            case 5:
                setFourCorners(dots, bounds);
                dots[4] = new PointF(bounds.centerX(), bounds.centerY());
                break;
            case 6:
                setFourCorners(dots, bounds);
                dots[4] = new PointF(bounds.left, bounds.centerY());
                dots[5] = new PointF(bounds.right, bounds.centerY());
                break;
        }
        return dots;
    }

    /**
     * Helper function for getting the four corners of bound.
     * @param dots Has to have length of at least 4.
     */
    private static void setFourCorners(PointF[] dots, RectF bound) {
        dots[0] = new PointF(bound.left, bound.top);
        dots[1] = new PointF(bound.right, bound.top);
        dots[2] = new PointF(bound.left, bound.bottom);
        dots[3] = new PointF(bound.right, bound.bottom);
    }

}
