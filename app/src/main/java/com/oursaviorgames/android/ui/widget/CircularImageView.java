package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.oursaviorgames.android.R;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class CircularImageView extends ImageView {

    private static final String TAG = makeLogTag(CircularImageView.class);

    private static float circularImageBorder = 1f;

    private final int fgColor;
    private final Paint holePaint;

    private Bitmap fgBitmap;

    public CircularImageView(Context context) {
        this(context, null, 0);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularImageView, 0, 0);
        fgColor = a.getInt(R.styleable.CircularImageView_foregroundColor, 0);

        holePaint = new Paint();
        holePaint.setAntiAlias(true);
        holePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        fgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fgBitmap);
        canvas.drawColor(fgColor);
        canvas.drawCircle(w / 2.f, w / 2.f, w / 2.f, holePaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(fgBitmap, 0.f, 0.f, null);
    }

}
