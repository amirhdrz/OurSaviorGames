package com.oursaviorgames.android.ui;

import android.content.Intent;
import android.os.Bundle;

import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.auth.CredentialServiceManager;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.backend.RoboService;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.AsyncSubject;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * An Activity loaded with {@link CredentialService} and binded to {@link RoboService}.
 */
public class AuthedActivity extends BaseRoboActivity implements CredentialServiceManager.Callback {

    private static final String TAG = makeLogTag(AuthedActivity.class);

    private final AsyncSubject<CredentialService> mCredServiceSubject = AsyncSubject.create();
    private final AsyncSubject<RoboHelper> mRoboHelperSubject = AsyncSubject.create();

    CredentialServiceManager mCredManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGD(TAG, "onCreate:: opening credential service");
        mCredManager = new CredentialServiceManager(this);
        mCredManager.onCreate(savedInstanceState);
        CredentialServiceManager.getCredentialService(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCredManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCredManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCredManager.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCredManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCredManager.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCredManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCredentialServiceLoaded(final CredentialService credentialService) {
        if (credentialService != null) {
            LOGD(TAG, "credential service loaded");
            mCredServiceSubject.onNext(credentialService);
            mCredServiceSubject.onCompleted();

            addSubscription(bindObservable(getRoboService())
                    .subscribe(new Action1<RoboService>() {
                        @Override
                        public void call(RoboService roboService) {
                            if (roboService != null) {
                                LOGD(TAG, "creating robohelper");
                                mRoboHelperSubject.onNext(new RoboHelper(roboService, credentialService));
                                mRoboHelperSubject.onCompleted();
                            } else {
                                mRoboHelperSubject.onError(new IllegalStateException("RoboService cannot be null"));
                            }
                        }
                    }));
        } else {
            mCredServiceSubject.onError(new IllegalStateException("CredentialService cannot be null"));
        }
    }

    /**
     * Returns this Activity's cached instance of {@link CredentialService}.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     */
    public Observable<CredentialService> getCredentialService() {
        return mCredServiceSubject;
    }

    /**
     * Returns this Activity's cached instance of {@link RoboHelper}.
     * <p>
     * <b>Observable:</b> This Observable is not bounded to Activity lifecycle.
     */
    public Observable<RoboHelper> getRoboHelper() {
        return mRoboHelperSubject;
    }

}
