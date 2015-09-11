package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.external.materialishprogress.Spinner;

/**
 * A View that only holds one child and can switch between the child and a progress spinner.
 */
public class ProgressBarSwitcher extends FrameLayout {

    // attrs spinnerSize enum values.
    public static final int SPINNER_SIZE_LARGE = 0;
    public static final int SPINNER_SIZE_SMALL = 1;

    // Flag indicating which view is shown.
    private boolean mSpinnerShown;
    private Spinner mSpinner;
    private View mChild;

    public ProgressBarSwitcher(Context context) {
        this(context, null);
    }

    public ProgressBarSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressBarSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBarSwitcher);
        final int spinnerSize = a.getInt(R.styleable.ProgressBarSwitcher_spinnerSize, SPINNER_SIZE_LARGE);
        a.recycle();

        mSpinner = new Spinner(getContext());
        mSpinner.setSize(spinnerSize);
        mSpinner.setVisibility(View.GONE);
        mSpinnerShown = false;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(mSpinner, 0, layoutParams);

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() >= 2) {
            throw new IllegalStateException("Can't add more than one child to this view");
        }
        // Spinner is explicitly at index zero.
        if (index != 0) {
            mChild = child;
        }
        super.addView(child, index, params);
    }

    /**
     * Shows spinner and hides and disables the child.
     * If spinner is already showing doesn't do anything.
     */
    public void showSpinner() {
        if (!mSpinnerShown) {
            mSpinner.setVisibility(View.VISIBLE);
            mChild.setVisibility(View.GONE);
            mChild.setEnabled(false);
            mSpinnerShown = true;
        }
    }

    /**
     * Hides spinner and shows and enables the child.
     * If spinner is already hidden, doesn't do anything.
     */
    public void hideSpinner() {
        if (mSpinnerShown) {
            mSpinner.setVisibility(View.GONE);
            mChild.setVisibility(View.VISIBLE);
            mChild.setEnabled(true);
            mSpinnerShown = false;
        }
    }

    private int mCount;

    /**
     * Shows spinner, but only hides it when {@link #doneLoading()}
     * is called {@code n} number of times.
     * This method resets the internal count.
     */
    public void showSpinnerMultiple(int n) {
        mCount = n;
        showSpinner();
    }

    /**
     * Hides spinner if this is called n number of times,
     * matching that last call to {@link #showSpinnerMultiple(int)}
     */
    public void doneLoading() {
        if (mCount > 0 && --mCount == 0) {
            hideSpinner();
        }
    }

}
