package com.oursaviorgames.android.ui;

import android.app.Activity;

import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.backend.RoboHelper;
import rx.Observable;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public abstract class AuthedRoboHelperFragment extends BaseRoboFragment {

    private static final String TAG = makeLogTag(AuthedRoboHelperFragment.class);

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof AuthedActivity)) {
            throw new ClassCastException(activity.toString()
                    + " must extend AuthedActivity");
        }
    }

    /**
     * Returns instance CredentialService Observable which is cached.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     */
    protected final Observable<CredentialService> getCredentialService() {
        return ((AuthedActivity) getActivity()).getCredentialService();
    }

    /**
     * Returns this fragment's instance of {@link RoboHelper} Observable which is cached.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     */
    protected final Observable<RoboHelper> getRoboHelper() {
        if (getActivity() == null) throw new NullPointerException("getActivity is null");
        return ((AuthedActivity) getActivity()).getRoboHelper();
    }

}
