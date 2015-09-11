package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.oursaviorgames.android.R;

public final class SwitchButton extends View {

    private static final String TEXT_FONT_PATH             = "fonts/Roboto-Regular.ttf";
    private static final int    SELECTED_TEXT_COLOR_RES_ID = R.color.white;
    private static final int    TEXT_COLOR_RES_ID          = R.color.switchButtonDefaultTextColor;
    private static final int    SELECTED_COLOR_RES_ID      = R.color.primaryDark;
    private static final float  STROKE_SIZE_DIP            = 1.f;
    private static final float  TEXT_SIZE_DIP              = 14.f;

    private static Paint sBorderPaint;
    private static Paint sSelectedTextPaint;
    private static Paint sTextPaint;
    private static Paint sFillPaint;
    private static float sStrokeWidth = 0.f;

    private NotifySwitchListener mNotifySwitchListener;
    private Path                 lPath;
    private Path                 rPath;
    private String mLText = "";
    private String mRText = "";
    private int    mLId   = 0;
    private int    mRId   = 1;

    private SwitchListener mSwitchListener;
    private int mActiveSwitchId = 0;
    private boolean mPressed;
    private int     w, h;
    private float halfW, halfH, textYOrigin;

    public SwitchButton(Context context) {
        super(context);
        init();
    }

    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (sStrokeWidth == 0.f) {
            sStrokeWidth = convertDipToPx(STROKE_SIZE_DIP);
        }
        if (sSelectedTextPaint == null) {
            sSelectedTextPaint = new Paint();
            sSelectedTextPaint.setColor(getContext().getResources().getColor(SELECTED_TEXT_COLOR_RES_ID));
            sSelectedTextPaint.setTextSize(convertDipToPx(TEXT_SIZE_DIP));
            sSelectedTextPaint.setAntiAlias(true);
            sSelectedTextPaint.setTextAlign(Paint.Align.CENTER);
            sSelectedTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), TEXT_FONT_PATH));
        }
        if (sTextPaint == null) {
            sTextPaint = new Paint(sSelectedTextPaint);
            sTextPaint.setColor(getContext().getResources().getColor(TEXT_COLOR_RES_ID));
        }
        if (sBorderPaint == null) {
            sBorderPaint = new Paint();
            sBorderPaint.setColor(getContext().getResources().getColor(SELECTED_COLOR_RES_ID));
            sBorderPaint.setStyle(Paint.Style.STROKE);
            sBorderPaint.setStrokeWidth(sStrokeWidth);
        }
        if (sFillPaint == null) {
            sFillPaint = new Paint(sBorderPaint);
            sFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

    }

    public void setOnSwitchListener(SwitchListener listener) {
        mSwitchListener = listener;
    }

    public void setTextAndIds(String lText, String rText, int lId, int rId, int defaultActive) {
        if (lText != null && rText != null) {
            mLText = lText;
            mRText = rText;
            mLId = lId;
            mRId = rId;
            mActiveSwitchId = defaultActive;
        }
    }

    public int getActiveSwitchId() {
        return mActiveSwitchId;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();

        switch(event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (mPressed) {
                    int clickedId = (x < halfW)? mLId : mRId;
                    if (clickedId != mActiveSwitchId) {
                        mActiveSwitchId = clickedId;
                        invalidate();
                        // Use a Runnable and post this rather than calling
                        // notifySwitchListener directly. This lets other visual state
                        // of the view update before click actions start.
                        if (mNotifySwitchListener == null) {
                            mNotifySwitchListener = new NotifySwitchListener();
                        }
                        if (!post(mNotifySwitchListener)) {
                            notifySwitchListener();
                        }
                    }
                    mPressed = false;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mPressed = true;
                break;
            default:
                return super.onTouchEvent(event);
        }
        return true;
    }

    private void notifySwitchListener() {
        if (mSwitchListener != null) {
            mSwitchListener.onSwitch(mActiveSwitchId);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w; this.h = h;
        halfW = w / 2.f;
        halfH = h / 2.f;

        // Calculates the Y-axis origin of the text.
        textYOrigin = calculateCenteredTextYOrigin(h, sTextPaint);

        float hairLine = sStrokeWidth;

        lPath = new Path();
        lPath.moveTo(hairLine, hairLine);
        lPath.lineTo(halfW + halfH, hairLine);
        lPath.lineTo(halfW - halfH, h - hairLine);
        lPath.lineTo(hairLine, h - hairLine);
        lPath.lineTo(hairLine, hairLine);

        rPath = new Path();
        rPath.moveTo(w - hairLine, hairLine);
        rPath.lineTo(w - hairLine, h - hairLine);
        rPath.lineTo(halfW - halfH, h - hairLine);
        rPath.lineTo(halfW + halfH, hairLine);
        rPath.lineTo(w - hairLine, hairLine);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mActiveSwitchId == mLId) {
            // draw dark paint on left.
            // draw dark border on right.
            canvas.drawPath(lPath, sFillPaint);
            canvas.drawPath(rPath, sBorderPaint);
            canvas.drawText(mLText, w * 0.25f, textYOrigin, sSelectedTextPaint);
            canvas.drawText(mRText, w * 0.75f, textYOrigin, sTextPaint);
        } else {
            // draw dark paint on right.
            // draw dark border on right.
            canvas.drawPath(lPath, sBorderPaint);
            canvas.drawPath(rPath, sFillPaint);
            canvas.drawText(mLText, w * 0.25f, textYOrigin, sTextPaint);
            canvas.drawText(mRText, w * 0.75f, textYOrigin, sSelectedTextPaint);
        }
    }

    private float convertDipToPx(float dips) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

    private final class NotifySwitchListener implements Runnable {
        @Override
        public void run() {
            notifySwitchListener();
        }
    }


    private static float calculateCenteredTextYOrigin(float containerHeight, Paint textPaint) {
        return (containerHeight / 2.f) +
                ((-textPaint.ascent() + textPaint.descent()) / 2.f) - textPaint.descent();
    }
    /**
     * Listener for SwitchButton events.
     */
    public interface SwitchListener {
        public void onSwitch(int activeId);
    }

}
