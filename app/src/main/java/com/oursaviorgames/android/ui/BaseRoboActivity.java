package com.oursaviorgames.android.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.backend.RoboService;
import rx.Observable;
import rx.subjects.AsyncSubject;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A subclass of {@link FragmentActivity} that binds to {@link RoboService},
 * and provides methods for {@link rx.Observable}s to bind themselves
 * to this Activity's lifecycle, and for {@link rx.Subscription}s
 * to unsubscribe when Activity's {@link #onDestroy()} is called.
 * It also provides utility functions such showing a loading screen.
 */
public abstract class BaseRoboActivity extends BaseActivity {

    private static final String TAG = makeLogTag(BaseRoboActivity.class);

    private AsyncSubject<RoboService> mRoboServiceSubject = AsyncSubject.create();

    // Number of calls to show LoadingFragment.
    private int showLoadingFragmentCount = 0;
    private int                   mContainerId;
    private Fragment              loadingFragment;
    private CredentialService     mActiveCredentialService;// TODO: deprecate this, only used by LoginActivity.
    private RoboService           mRoboService;
    private boolean               mBound;

    @Override
    protected void onStart() {
        super.onStart();
        // Binds to RoboService
        Intent intent = new Intent(this, RoboService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    /**
     * Sets the active {@link CredentialService}.
     */
    //TODO: deprecate this. Should use UiLifecycleHelper
    @Deprecated
    protected void setActiveCredentialService(CredentialService service) {
        LOGD(TAG, "Active CredentialService set");
        mActiveCredentialService = service;
    }

    /**
     * Returns currently active {@link CredentialService}, or null
     * if no active service has been set.
     */
    //TODO: deprecate this. Should use UiLifecycleHelper
    @Deprecated
    public final CredentialService getActiveCredentialService() {
        return mActiveCredentialService;
    }

    /**
     * Returns bounded {@link com.oursaviorgames.android.backend.RoboService},
     * null if the service is not bounded.
     */
    @Deprecated
    public final RoboService getRoboService2() {
        if (mBound) {
            return mRoboService;
        } else {
            throw new IllegalStateException("RoboService not bound");
        }
    }

    /**
     * Returns a bounded Observable that emits {@link RoboService}.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     */
    public final Observable<RoboService> getRoboService() {
        return mRoboServiceSubject;
    }

    /**
     * Whether the loading screen is being shown or not.
     */
    protected final boolean isLoadingOn() {
        return (showLoadingFragmentCount > 0);
    }

    protected final void setFragmentContainerId(int containerResId) {
        mContainerId = containerResId;
    }

    /**
     * Shows {@link LoadingFragment} on the provided container.
     */
    protected final void showLoading() {
        if (showLoadingFragmentCount == 0) {
            if (loadingFragment == null) {
                loadingFragment = LoadingFragment.newInstance();
            }
            LOGD(TAG, "showloading attempt");
            // perform the transaction and show the fragment.
            getSupportFragmentManager().beginTransaction()
                    .add(mContainerId, loadingFragment)
                    .commit();
        }
        showLoadingFragmentCount++;
    }

    /**
     * To hide the loadingFragment, the number of pushes
     * must match the number of pops.
     */
    protected final void hideLoading() {
        LOGD(TAG, "HideLoading. count: " + showLoadingFragmentCount);
        if (showLoadingFragmentCount > 0) {
            if (--showLoadingFragmentCount == 0) {
                getSupportFragmentManager().beginTransaction()
                        .remove(loadingFragment)
                        .commit();
            }
        }
    }

    /** ServiceConnection to {@link RoboService} */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LOGD(TAG, "Connected to RoboService");
            RoboService.RoboticBinder binder = (RoboService.RoboticBinder) service;
            mRoboService = binder.getService();
            mBound = true;
            mRoboServiceSubject.onNext(mRoboService);
            mRoboServiceSubject.onCompleted();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LOGD(TAG, "Disconnected from RoboService");
            mBound = false;
            mRoboServiceSubject = null;
        }
    };

}
