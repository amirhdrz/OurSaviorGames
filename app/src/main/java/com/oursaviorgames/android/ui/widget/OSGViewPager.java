package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.oursaviorgames.android.ui.OSGViewPagerAdapter;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A ViewPager that only hooks up with {@link OSGViewPagerAdapter}.
 */
public class OSGViewPager extends ViewPager {

    private static final String TAG = makeLogTag(OSGViewPager.class);

    public OSGViewPager(Context context) {
        super(context);
    }

    public OSGViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAdapter(OSGViewPagerAdapter adapter) {
        super.setAdapter(adapter);
    }

    public OSGViewPagerAdapter getAdapter() {
        return (OSGViewPagerAdapter) super.getAdapter();
    }

}
