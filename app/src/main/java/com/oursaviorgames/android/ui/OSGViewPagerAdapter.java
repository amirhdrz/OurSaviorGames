package com.oursaviorgames.android.ui;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.oursaviorgames.android.R;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class OSGViewPagerAdapter extends PagerAdapter {

    private static final String TAG = makeLogTag(OSGViewPagerAdapter.class);

    private final Context mContext;
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mCurTransaction     = null;
    private Fragment            mCurrentPrimaryItem = null;

    public OSGViewPagerAdapter(Context context, FragmentManager fm) {
        mContext = context;
        mFragmentManager = fm;
    }

    /**
     * Return the Fragment associated with a specified position.
     */
    @Override
    public void startUpdate(ViewGroup container) {
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final long itemId = getItemId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(container.getId(), itemId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                    makeFragmentName(container.getId(), itemId));
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        mCurTransaction.detach((Fragment)object);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        LOGD(TAG, "setPrimaryItem:: position" + position);
        Fragment fragment = (Fragment)object;
        if (fragment != mCurrentPrimaryItem) {
            LOGD(TAG, "setPrimaryItem:: primary item changed");
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem.setMenuVisibility(false);
                mCurrentPrimaryItem.setUserVisibleHint(false);
                if (mCurrentPrimaryItem instanceof ViewPagerFragment) {
                    ((ViewPagerFragment) mCurrentPrimaryItem).performVisibilityChanged(false);
                } else {
                    throw new IllegalStateException("Fragment must be a subclass of ViewPagerFragment");
                }
            }
            if (fragment != null) {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
                ((AuthedRoboHelperFragment) fragment).performVisibilityChanged(true);
            }
            mCurrentPrimaryItem = fragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment)object).getView() == view;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    /**
     * Return a unique identifier for the item at the given position.
     *
     * <p>The default implementation returns the given position.
     * Subclasses should override this method if the positions of items can change.</p>
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    public long getItemId(int position) {
        return position;
    }

    private static String makeFragmentName(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }



    /*
        My Implementation.
     */

    public void onResume() {
        LOGD(TAG, "onResume:: mCurrentPrimaryItem ?= null: " + (mCurrentPrimaryItem == null));
        if (mCurrentPrimaryItem != null) {
            ((AuthedRoboHelperFragment) mCurrentPrimaryItem).performVisibilityChanged(true);
        }
    }

    public void onPause() {
        LOGD(TAG, "onPause:: mCurrentPrimaryItem ?= null: " + (mCurrentPrimaryItem == null));
        if (mCurrentPrimaryItem != null) {
            ((AuthedRoboHelperFragment) mCurrentPrimaryItem).performVisibilityChanged(false);
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    /**
     * Returns tab indicator for given tab position.
     */
    public View getIndicator(int position) {
        ImageView tabIndicator = new ImageView(mContext);
        tabIndicator.setScaleType(ImageView.ScaleType.CENTER);
        switch (position) {
            case 0:
                tabIndicator.setImageResource(R.drawable.ic_home_white_36dp);
                break;
            case 1:
                tabIndicator.setImageResource(R.drawable.ic_explore_white_36dp);
                break;
            case 2:
                tabIndicator.setImageResource(R.drawable.ic_favorite_white_36dp);
                break;
            case 3:
                tabIndicator.setImageResource(R.drawable.ic_menu_white_36dp);
                break;
        }
        return tabIndicator;
    }

    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = HomeFragment.newInstance();
                break;
            case 1:
                fragment = ExploreFragment.newInstance();
                break;
            case 2:
                fragment = FavoritesFragment.newInstance();
                break;
            default:
                fragment = MenuFragment.createInstance();
        }
        return fragment;
    }

}
