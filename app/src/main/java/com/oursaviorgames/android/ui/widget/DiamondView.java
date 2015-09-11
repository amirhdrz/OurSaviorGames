package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.oursaviorgames.android.R;

/**
 * Trapezoid shaped
 */
public class DiamondView extends FrameLayout {

    private Paint bgPaint;
    private Path  osgPath;

    public DiamondView(Context context) {
        this(context, null);
    }

    public DiamondView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiamondView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DiamondView, 0, 0);

        // An alpha value from 0.f to 1.f
        float alpha = a.getFloat(R.styleable.DiamondView_backgroundAlpha, 1.0f);

        // Background color. If not set, we only use black with set alpha value
        int bgColor = a.getInt(R.styleable.DiamondView_backgroundColor, -1);

        // Recycle type array.
        a.recycle();

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        if (bgColor == -1) {
            bgPaint.setColor(Color.BLACK);
            bgPaint.setAlpha((int) (alpha * 256));
        } else {
            bgPaint.setColor(bgColor);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            osgPath = new Path();
            osgPath.moveTo(h, 0.f);
            osgPath.lineTo(w, 0.f);
            osgPath.lineTo(w - h, h);
            osgPath.lineTo(0.f, h);
            osgPath.lineTo(0.f, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(osgPath, bgPaint);
    }

}
