package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.TypefaceUtils;
import com.oursaviorgames.android.util.UiUtils;

/**
 *  TextView that uses modern Roboto fonts packaged in 'assets/fonts'.
 *  Use attribute 'app:fontStyle' to select which Roboto font to use,
 *  defaults to Roboto Regular.
 *
 */
public class RoboTextView extends TextView {

    public RoboTextView(Context context) {
        super(context);
    }

    public RoboTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoboTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RoboTextView,
                0, 0);

        try {
            int fontStyle = a.getInt(R.styleable.RoboTextView_fontStyle, 2);
            Typeface typeface = TypefaceUtils.getTypeface(context, fontStyle);
            setTypeface(typeface);
        } finally {
            a.recycle();
        }
    }
}
