package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class DropShadowView extends View {

    private static final float FIRST_LAYER_HEIGHT_DIPS = 4.f;

    private Paint mFirstLayerPaint;
    private Paint mSecondLayerPaint;
    private final int mFirstLayerHeight;
    private int   w, h;

    public DropShadowView(Context context) {
        this(context, null);
    }

    public DropShadowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DropShadowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFirstLayerHeight = (int) convertDipToPx(FIRST_LAYER_HEIGHT_DIPS);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.w = w;
        this.h = h;

        final int startColor = addAlpha(0.2f, Color.BLACK);
        final int endColor = Color.TRANSPARENT;

        // Sets the first layer shader.
        Shader shadow1 = new LinearGradient(0.f, 0.f, 0.f, mFirstLayerHeight,
                startColor, endColor, Shader.TileMode.REPEAT);
        mFirstLayerPaint = new Paint();
        mFirstLayerPaint.setAntiAlias(true);
        mFirstLayerPaint.setStyle(Paint.Style.FILL);
        mFirstLayerPaint.setShader(shadow1);

        // Sets the second layer shader.
        Shader shadow2 = new LinearGradient(0.f, 0.f, 0.f, h,
                startColor, endColor, Shader.TileMode.REPEAT);
        mSecondLayerPaint = new Paint(mFirstLayerPaint);
        mSecondLayerPaint.setShader(shadow2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0.f, 0.f, w, mFirstLayerHeight, mFirstLayerPaint);
        canvas.drawRect(0.f, 0.f, w, h, mSecondLayerPaint);
    }

    /**
     * Adds alpha level to given opaque color.
     * @param level value in range [0..1)
     * @param opaqueColor any color, but it'salpha is removed.
     */
    private static int addAlpha(float level, int opaqueColor) {
        int opacity = (int) (level * 256);
        return (opacity << 24) | (0x00ffffff & opaqueColor);
    }

    private float convertDipToPx(float dips) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

}
