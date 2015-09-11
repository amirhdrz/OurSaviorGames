package com.oursaviorgames.android.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;

import butterknife.ButterKnife;
import butterknife.InjectView;

import com.facebook.AppEventsLogger;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.cache.VideoThumbnailCache;
import com.oursaviorgames.android.data.VideoProvider;
import com.oursaviorgames.android.ui.widget.OSGViewPager;
import com.oursaviorgames.android.ui.widget.TabLayout;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

//TODO: use a job scheduler to sync events.
//            SyncService.startSyncAll(this);
public class MainActivity extends AuthedActivity {

    private final static String TAG = makeLogTag(MainActivity.class);

    @InjectView(R.id.tabLayout)
    TabLayout    mTabLayout;
    @InjectView(R.id.viewPager)
    OSGViewPager mViewPager;

    OSGViewPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // As the host activity, we initialize commonly used services.
        VideoProvider.openProvider(this);
        VideoThumbnailCache.getInstance(this);

        mPagerAdapter = new OSGViewPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabLayout.setViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        // PagerAdapter needs to be notified before Fragments.
        mPagerAdapter.onResume();
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        // PagerAdapter needs to be notified before Fragments.
        mPagerAdapter.onPause();
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoProvider.closeOpenedProvider();
        VideoThumbnailCache.clearInstanceAndPurgeCache();
    }

}
