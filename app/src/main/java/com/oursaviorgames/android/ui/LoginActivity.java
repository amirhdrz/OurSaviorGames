package com.oursaviorgames.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.File;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.auth.FacebookCredentialService;
import com.oursaviorgames.android.auth.GetUserInfoException;
import com.oursaviorgames.android.auth.GoogleCredentialService;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.backend.RoboService;
import com.oursaviorgames.android.backend.processor.ForbiddenException;
import com.oursaviorgames.android.backend.processor.ProcessorIOException;
import com.oursaviorgames.android.backend.processor.UnauthorizedException;
import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.rx.NullFunc;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.FileUtils;
import com.oursaviorgames.android.util.LogUtils;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.oursaviorgames.android.util.LogUtils.LOGD;

/**
 * Starts the authentication flow.
 * Note that this Activity clears out data stored in {@link UserAccount}.
 */
//TODO: bug when multiple signup fragments are loaded if activity is stopped and started again.
public class LoginActivity extends BaseRoboActivity
implements HelloFragment.HelloFragmentListener,
            CredentialService.StateListener,
            UserSignupFragment.UserSignUpFragmentListener {

    private static final String TAG = LogUtils.makeLogTag(LoginActivity.class);

    CredentialService fb, gPlus;
    CredentialService            mSelectedProvider;
    boolean                      mInternalAuthInProgress;
    Uri                          mProfilePicDestUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_login);
        setFragmentContainerId(R.id.container);

        // Reset saved account data.
        // We don't want any old data to contaminate requests
        // made from this Activity.
        UserAccount.getUserAccount(this).clearAccountData();

        fb = new FacebookCredentialService();
        gPlus = new GoogleCredentialService();

        fb.setListener(this);
        fb.onCreate(this);
        gPlus.setListener(this);
        gPlus.onCreate(this);

        // Instantiate member fields
        //TODO: this file needs to be cleaned up
        mProfilePicDestUri = Uri.fromFile(new File(FileUtils.getDir(this, FileUtils.DIR.USER), "dl-userprofile"));

        // Shows hello fragment
        Fragment helloFragment = HelloFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, helloFragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fb.onStart();
        gPlus.onStart();
        if (isLoadingOn()) {
            hideLoading();
            // All old Observables have been halted, so
            // we can safely reset the internal auth flag.
            mInternalAuthInProgress = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fb.onStop();
        gPlus.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOGD(TAG, "Activity result called with code (" + requestCode + ") and result code (" + resultCode + ")");
        fb.onActivityResult(requestCode, resultCode, data);
        gPlus.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onIdentityProviderSelected(final UserAccount.IdentityProvider identityProvider) {
        switch (identityProvider) {
            case FACEBOOK:
                mSelectedProvider = fb;
                break;
            case GOOGLE_PLUS:
                // Before performing auth, check if google play services is available.
                int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
                if (errorCode != ConnectionResult.SUCCESS) {
                    GooglePlayServicesUtil.showErrorNotification(errorCode, this);
                    return;
                } else {
                    mSelectedProvider = gPlus;
                    break;
                }
        }
        if (mSelectedProvider.getState() == CredentialService.State.RESOLVABLE_EXCEPTION
                || mSelectedProvider.getState() == CredentialService.State.FAILED) {
            LOGD(TAG, "Resolving signin exception");
            mSelectedProvider.resolveException();
        } else if (mSelectedProvider.getState() == CredentialService.State.TOKEN_LOADED) {
            // Access token is already available,
            // can perform internal authentication now.
            // we have to call onStateChanged manually.
            onStateChanged(mSelectedProvider, mSelectedProvider.getState());
        }
    }

    /**
     * This function is implemented, in case the user selects a provider,
     * before any of the providers have a chance to load their token (if they have any).
     * Contains the logic to call {@code performInternalAuthentication}, so this method
     * should be called manually in case the user selected a provider after the provider
     * has loaded their token.
     */
    @Override
    public void onStateChanged(CredentialService service, CredentialService.State state) {
        LOGD(TAG, "CredentialService: " + service.getName().toString()
                + " changed state to: " + state.toString());
        if (mSelectedProvider != null) LOGD(TAG, "Currently selected provider" + mSelectedProvider.getName());
        if (mSelectedProvider != null && mSelectedProvider.getName() == service.getName()) {
            // If the token is loaded, proceed to internal authentication
            // otherwise try to resolve the exception.
            if (state == CredentialService.State.TOKEN_LOADED) {
                // Clear the selected provider flag.
                mSelectedProvider = null;
                // We're only interested in the IdentityProvider that the user has selected.
                setActiveCredentialService(service);
                // User has resolved the exception,
                // can perform internal authentication now.
                LOGD(TAG, "Calling to perform internal authentication");
                performInternalAuthentication(service);
            } else if (state == CredentialService.State.RESOLVABLE_EXCEPTION) {
                // don't clear the selected provider flag here.
                // we need it for when the exception is resolved and this function called again.
                mSelectedProvider.resolveException();
            }
        }
    }

    // this method might be called several times. keep an internal flag.
    private void performInternalAuthentication(final CredentialService selectedService) {
        if (!mInternalAuthInProgress) {
            LOGD(TAG, "Performing internal authentication");
            // Store selected provider to be used to be signup run if needed.
            mInternalAuthInProgress = true;
            showLoading();

            addSubscription(bindObservable(
                    getRoboService()
                    .flatMap(new Func1<RoboService, Observable<Void>>() {
                        @Override
                        public Observable<Void> call(RoboService roboService) {
                            final RoboHelper robo = new RoboHelper(roboService, selectedService);
                            return robo.getUserProfile()
                                    .flatMap(new Func1<Boolean, Observable<Void>>() {
                                        @Override
                                        public Observable<Void> call(Boolean gotProfile) {
                                            String profileUrl = UserAccount.getUserAccount(LoginActivity.this).getProfileImageUrl();
                                            LOGD(TAG, "downloading user profile image: " + profileUrl);
                                            //TODO: ugly. do it better.
                                            if (profileUrl != null) {
                                                return robo.downloadFile(profileUrl, Uri.fromFile(FileUtils.getProfilePictureFile(LoginActivity.this)))
                                                        .map(new NullFunc<Uri>());
                                            } else {
                                                return Observable.just(null);
                                            }
                                        }
                                    });
                        }
                    }))

                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            mInternalAuthInProgress = false;
                            if (e instanceof UnauthorizedException) {
                                // Start the signup run
                                showLoading();
                                startSignUpProcess(selectedService);
                            } else if (e instanceof ProcessorIOException) {
                                ErrorUtils.showNetworkError(LoginActivity.this);
                            } else {
                                e.printStackTrace();
                                ErrorUtils.showError(LoginActivity.this, R.string.error_generic_login);
                            }
                            hideLoading();
                        }

                        @Override
                        public void onNext(Void aVoid) {
                            mInternalAuthInProgress = false;
                            setResult(RESULT_OK);
                            finish();
                        }
                    }));
        }
    }

    private void startSignUpProcess(final CredentialService selectedService) {
        Observable.combineLatest(getRoboService(), selectedService.requestUserInfo(),
                new Func2<RoboService, CredentialService.UserInfo, Observable<UserSignupFragment>>() {
                    @Override
                    public Observable<UserSignupFragment> call(RoboService roboService, final CredentialService.UserInfo userInfo) {
                        RoboHelper robo = new RoboHelper(roboService, selectedService);
                        return Observable.combineLatest(robo.generateUsername(userInfo.name),
                                robo.downloadFile(userInfo.profileImageUrl, mProfilePicDestUri),
                                new Func2<String, Uri, UserSignupFragment>() {
                                    @Override
                                    public UserSignupFragment call(String s, Uri uri) {
                                        return UserSignupFragment.newInstance(userInfo.name, s, userInfo.gender, uri);
                                    }
                                });
                    }
                })
        .flatMap(new Func1<Observable<UserSignupFragment>, Observable<UserSignupFragment>>() {
            @Override
            public Observable<UserSignupFragment> call(Observable<UserSignupFragment> userSignupFragmentObservable) {
                return userSignupFragmentObservable;
            }
        })
        .subscribe(new AndroidSubscriber<UserSignupFragment>() {
            @Override
            public boolean onError2(Throwable e) {
                LOGD(TAG, "Deepshit error of type: " + e.toString());
                if (LogUtils.DEBUG) {
                    e.printStackTrace();
                }

                if (e instanceof UnauthorizedException
                    || e instanceof ForbiddenException) {
                    ErrorUtils.showError(LoginActivity.this, R.string.error_generic_login);
                    LOGD(TAG, "Closing and clearing token caches");
                    fb.closeAndClearTokenInformation();
                    gPlus.closeAndClearTokenInformation();
                    // recreating the activity.
                    recreate();
                } else if (e instanceof GetUserInfoException) {
                    ErrorUtils.showError(LoginActivity.this, R.string.error_retrieve_user_info);
                } else {
                    //TODO: should really do better error checking.
                    ErrorUtils.showNetworkError(LoginActivity.this);
                }
                // We're done. Hide loading screen.
                hideLoading();
                return true;
            }
            @Override
            public void onNext(UserSignupFragment userSignupFragment) {
                // Puts the fragment onto the container.
                hideLoading();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, userSignupFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public void onUserSignUpFragmentInteraction(int interaction) {
        if (UserSignupFragment.UserSignUpFragmentListener.USER_SIGNED_UP == interaction) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
