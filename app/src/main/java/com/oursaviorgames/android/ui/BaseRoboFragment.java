package com.oursaviorgames.android.ui;

import android.app.Activity;

import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.backend.RoboService;
import rx.Observable;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;


//TODO: all activity lifecycle methods should be moved to BaseFragment.
public abstract class BaseRoboFragment extends ViewPagerFragment {

    private static final String TAG = makeLogTag(BaseRoboFragment.class);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof BaseRoboActivity)) {
            throw new ClassCastException(activity.toString()
                    + " must extend BaseRoboActivity");
        }
    }

    /**
     * Returns active {@link CredentialService} set by this fragment's
     * Activity.
     * @return active {@link CredentialService}, or null if it is not set.
     */
    @Deprecated
    public CredentialService getActiveCredentialService() {
        return ((BaseRoboActivity) getActivity()).getActiveCredentialService();
    }

    /**
     * Returns {@link com.oursaviorgames.android.backend.RoboService} bounded
     * to this Fragment's Activity.
     * @return {@link RoboService}, or null if the service is not bounded.
     */
    @Deprecated
    public RoboService getRoboService2() {
        return ((BaseRoboActivity) getActivity()).getRoboService2();
    }

    /**
     * Returns a bounded Observable that emits {@link RoboService}.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     * @return
     */
    public Observable<RoboService> getRoboService() {
        return ((BaseRoboActivity) getActivity()).getRoboService();
    }

}
