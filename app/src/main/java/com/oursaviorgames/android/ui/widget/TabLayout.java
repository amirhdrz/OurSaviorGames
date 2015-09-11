package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.ui.OSGViewPagerAdapter;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * This class uses {@link OSGViewPager} only as its ViewPager.
 */
public class TabLayout extends LinearLayout {

    private static final String TAG = makeLogTag(TabLayout.class);

    private static final int   STRIPE_COLOR_RES_ID = R.color.accent;
    private static final float STRIPE_WIDTH_DIPS   = 1.f;
    private static final float STRIPE_MARGIN_DIPS = 3.f;

    private static Paint  sStripePaint;

    private float mStripeWidth;
    private float mStripeMargin;
    private float mUnitStripeWidth; // width of a whole unit of a stripe, thin stroke and margin.
    private int   w, h;
    private int   mStripeCount;

    private OSGViewPager                   mViewPager;
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private final TabStrip mTabStrip;

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mTabStrip = new TabStrip(context);
        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        mStripeWidth = convertDipToPx(STRIPE_WIDTH_DIPS);
        mStripeMargin = convertDipToPx(STRIPE_MARGIN_DIPS);
        mUnitStripeWidth = mStripeWidth + mStripeMargin;

        if (sStripePaint == null) {
            sStripePaint = new Paint();
            sStripePaint.setAntiAlias(true);
            sStripePaint.setColor(context.getResources().getColor(STRIPE_COLOR_RES_ID));
            sStripePaint.setStrokeWidth(mStripeWidth);
        }
    }

    /**
     * Set the {@link android.support.v4.view.ViewPager.OnPageChangeListener}. When using {@link TabLayout} you are
     * required to set any {@link android.support.v4.view.ViewPager.OnPageChangeListener} through this method. This is so
     * that the layout can update it's scroll position correctly.
     *
     * @see android.support.v4.view.ViewPager#setOnPageChangeListener(android.support.v4.view.ViewPager.OnPageChangeListener)
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }


    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(OSGViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private void populateTabStrip() {
        // Sets Tab click listener for each tab.
        final OnClickListener tabClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                    if (v == mTabStrip.getChildAt(i)) {
                        mViewPager.setCurrentItem(i);
                        return;
                    }
                }
            }
        };
        final OSGViewPagerAdapter adapter = mViewPager.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView = adapter.getIndicator(i);
            tabView.setOnClickListener(tabClickListener);
            LayoutParams layoutParams =
                    new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.f);
            mTabStrip.addView(tabView, layoutParams);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        mStripeCount = (int) ((w + h) / (mStripeWidth + mStripeMargin));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draws stripe pattern.
        for (int i = 0; i < mStripeCount; i++) {
            canvas.drawLine(i * mUnitStripeWidth, 0.f, i * mUnitStripeWidth - h, h, sStripePaint);
        }
    }

    private float convertDipToPx(float dips) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips,
                getResources().getDisplayMetrics());
    }

    //Internal ViewPager listener.
    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

    }

}
