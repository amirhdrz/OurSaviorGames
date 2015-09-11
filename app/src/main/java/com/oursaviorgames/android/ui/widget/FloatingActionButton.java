                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.UiUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class FloatingActionButton extends View implements AbsListView.OnScrollListener{

    private static final String TAG = makeLogTag(FloatingActionButton.class);

    private static final long ANIMATION_DURATION = 250l;
    private static final float SHADOW_RADIUS_DIPS        = 2.f;
    private static final float SHADOW_DX_DIPS            = 0.f;
    private static final float SHADOW_DY_DIPS            = 2.f;
    private static final int   SHADOW_COLOR_RES_ID       = R.color.shadow;
    private static final float BUTTON_RADIUS_DIPS        = 28.f;
    private static final float VIEW_WIDTH_DIPS           = (BUTTON_RADIUS_DIPS + SHADOW_RADIUS_DIPS) * 2 + SHADOW_DX_DIPS;
    private static final float VIEW_HEIGHT_DIPS          = VIEW_WIDTH_DIPS + SHADOW_DY_DIPS + SHADOW_RADIUS_DIPS;
    private static final int   BG_COLOR_RES_ID           = R.color.primary;

    private final Paint        mButtonPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int      halfW;
    private float    mButtonRadius;
    private Drawable mDrawable;
    private int      mBgColor;
    private boolean mHidden        = false;
    private boolean mYDisplayedSet = false;

    /**
     * The button's Y position when it is displayed.
     */
    private float mYDisplayed = -1;
    /**
     * The button's Y position when it is hidden.
     */
    private float mYHidden    = -1;

    public FloatingActionButton(Context context) {
        this(context, null);
    }

    public FloatingActionButton(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }


    public FloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBgColor = context.getResources().getColor(BG_COLOR_RES_ID);
        int shadowColor = context.getResources().getColor(SHADOW_COLOR_RES_ID);

        final float shadowRadius = UiUtils.convertDipToPx(context, SHADOW_RADIUS_DIPS);
        final float shadowDx = UiUtils.convertDipToPx(context, SHADOW_DX_DIPS);
        final float shadowDy = UiUtils.convertDipToPx(context, SHADOW_DY_DIPS);
        mButtonPaint.setStyle(Paint.Style.FILL);
        mButtonPaint.setColor(mBgColor);
        mButtonPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);
        setLayerType(LAYER_TYPE_SOFTWARE, mButtonPaint);

        // Gets the Y position when this View is hidden, based on the height of the screen.
        WindowManager mWindowManager = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mYHidden = size.y;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.halfW = w / 2;
        mButtonRadius = UiUtils.convertDipToPx(getContext(), BUTTON_RADIUS_DIPS);
        final int padding = (w - mDrawable.getIntrinsicWidth()) / 2;
        int drawableW = (mDrawable.getIntrinsicWidth() == -1)? 0 : mDrawable.getIntrinsicWidth();
        int drawableH = (mDrawable.getIntrinsicHeight() == -1)? 0 : mDrawable.getIntrinsicHeight();
        Rect drawableBounds = new Rect(
                padding,
                padding,
                padding + drawableW,
                padding + drawableH
        );
        mDrawable.setBounds(drawableBounds);
    }

    public void setColor(int color) {
        mBgColor = color;
        mButtonPaint.setColor(color);
        invalidate();
    }

    /**
     * Supports Animatable Drawable.
     */
    public void setDrawable(Drawable drawable) {
        if (mDrawable != drawable) {
            mDrawable = drawable;
            mDrawable.setCallback(this);
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        int color = 0;
        final boolean handled;
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                color = UiUtils.darkenColor(mBgColor);
                handled =true;
                break;
            case MotionEvent.ACTION_UP:
                color = mBgColor;
                handled =true;
                break;
            case MotionEvent.ACTION_CANCEL:
                color = mBgColor;
                handled =true;
                break;
            default:
                handled = false;
                break;
        }
        if (handled) {
            mButtonPaint.setColor(color);
            invalidate();
        }
        return super.onTouchEvent(event) || handled;
    }

    public void setHidden(boolean hidden) {
        if (mHidden != hidden) {
            mHidden = hidden;
            if (mHidden) {
                animate().y(mYHidden).alpha(0.f).setDuration(ANIMATION_DURATION).start();
                if (mDrawable instanceof Animatable) {
                    ((Animatable) mDrawable).stop();
                }
            } else {
                animate().y(mYDisplayed).alpha(1.f).setDuration(ANIMATION_DURATION).start();
                if (mDrawable instanceof Animatable) {
                    ((Animatable) mDrawable).start();
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = UiUtils.convertDipToPx(getContext(), VIEW_WIDTH_DIPS);
        final int height = UiUtils.convertDipToPx(getContext(), VIEW_HEIGHT_DIPS);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LOGD(TAG, "onLayout:: Y: " + getY() + ", mHidden: " + mHidden);
        if (!mYDisplayedSet) {
            mYDisplayed = getY();
            mYDisplayedSet = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(halfW, halfW, mButtonRadius, mButtonPaint);
        if (mDrawable != null) {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        super.invalidateDrawable(drawable);
        invalidate(drawable.getBounds());
    }


    /*
        --------------------------
        Scrolling behaviour
        --------------------------
     */
    private static final int DIRECTION_CHANGE_THRESHOLD = 48;
    private int mThresholdCount = 0;
    private int mPrevPosition;
    private int mPrevTop;
    private boolean mUpdated;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final View topChild = view.getChildAt(0);
        int firstViewTop = 0;
        if (topChild != null) {
            firstViewTop = topChild.getTop();
        }
        boolean goingDown;
        boolean changed = true;
        if (mPrevPosition == firstVisibleItem) {
            final int topDelta = mPrevTop - firstViewTop;
            goingDown = firstViewTop < mPrevTop;
            mThresholdCount += topDelta;
            LOGD(TAG, "topDelta:" + topDelta + ", mThresholdCount:" + mThresholdCount);
            changed = Math.abs(mThresholdCount) > DIRECTION_CHANGE_THRESHOLD;
        } else {
            goingDown = firstVisibleItem > mPrevPosition;
        }
        if (changed && mUpdated) {
            mThresholdCount = 0;
            setHidden(goingDown);
        }
        mPrevPosition = firstVisibleItem;
        mPrevTop = firstViewTop;
        mUpdated = true;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (AbsListView.OnScrollListener.SCROLL_STATE_IDLE == scrollState) {
            mThresholdCount = 0;
        }
    }

}