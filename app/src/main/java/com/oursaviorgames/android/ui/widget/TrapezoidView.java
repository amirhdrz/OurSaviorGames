package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.oursaviorgames.android.R;

public class TrapezoidView extends View {

    private static final int MASK_RIGHT_EDGE = 0x3;
    private static final int MASK_LEFT_EDGE  = 0xC;

    public static final int FLAG_RIGHT_VERTICAL = 0x0; // '|'
    public static final int FLAG_RIGHT_BACKWARD = 0x1; // '\'
    public static final int FLAG_RIGHT_FORWARD  = 0x2;  // '/'

    public static final int FLAG_LEFT_VERTICAL = 0x0; // '|'
    public static final int FLAG_LEFT_BACKWARD = 0x4; // '\'
    public static final int FLAG_LEFT_FORWARD  = 0x8;  // '/'

    private int   mEdgeFlags;
    private Path  mPath;
    private Paint mBgPaint;

    public TrapezoidView(Context context) {
        this(context, null);
    }

    public TrapezoidView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrapezoidView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setStyle(Paint.Style.FILL);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TrapezoidView,
                0, 0 );

        // Gets attributes
        mEdgeFlags = a.getInt(R.styleable.TrapezoidView_edgeStyle, 0);
        mBgPaint.setColor(a.getColor(R.styleable.TrapezoidView_contentBackground, 0));

        a.recycle();
    }

    public void setEdgeStyle(int edgeStyle) {
        mEdgeFlags = edgeStyle;
    }

    public void setContentBackground(int colorResId) {
        mBgPaint.setColor(getContext().getResources().getColor(colorResId));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mPath = new Path();

        final int leftEdgeFlag = mEdgeFlags & MASK_LEFT_EDGE;
        final int rightEdgeFlag = mEdgeFlags & MASK_RIGHT_EDGE;

        mPath.moveTo((leftEdgeFlag == FLAG_LEFT_FORWARD)    ? h       : 0, 0);
        mPath.lineTo((rightEdgeFlag == FLAG_RIGHT_BACKWARD) ? (w - h) : w, 0);
        mPath.lineTo((rightEdgeFlag == FLAG_RIGHT_FORWARD)  ? (w - h) : w, h);
        mPath.lineTo((leftEdgeFlag == FLAG_LEFT_BACKWARD)   ? h       : 0, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mBgPaint);
    }

}
