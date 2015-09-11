package com.oursaviorgames.android.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.ui.LoginActivity;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Loads and attaches lifecycle of this user's {@link CredentialService} with
 * attached Activity.
 *
 * Fragments only need to call {@link #getCredentialService(Activity, Callback)},
 * in order get the instance in the onCredentialServiceLoaded. Each fragment is only called once,
 * exactly for each time they call getCredentialService.
 * If a fragment wants to cancel the request and remove itself from the onCredentialServiceLoaded,
 * then it should call {@link #removeCallback(Activity, Callback)}.
 */
public class CredentialServiceManager implements CredentialService.StateListener{

    private static final String TAG = makeLogTag(CredentialServiceManager.class);

    private static final int REQUEST_ID_LOGIN = 0xbabe;

    // i'm assuming Activity hashCode function is properly implemented.
    private final static Map<Activity, CredentialServiceManager> sInstances = new HashMap<>(3, 1.f);

    private       Activity          mActivity;
    private       List<Callback>    mCallbacks;
    private       boolean           isResumed;
    private       State             mState;
    private       CredentialService mCredentialService;

    /**
     * Calls {@code onCredentialServiceLoaded} immediately, if CredentialService is loaded with token.
     * Otherwise {@code onCredentialServiceLoaded} is called asynchronously when CredentialService
     * is loaded with a valid token.
     * If this method is called from a Fragment and the containing Activity is not associated
     * with an instance of {@link CredentialServiceManager}, throws error.
     * Therefor calling this method is only safe when {@code activity} onCreate() method of has returns.
     * @param activity Containing Activity.
     * @param callback Callback to call once the {@link CredentialService} has been loaded.
     */
    public static void getCredentialService(Activity activity, Callback callback) {
        final CredentialServiceManager csm = sInstances.get(activity);
        if (csm != null) {
            if (csm.mState == State.OPEN) {
                LOGD(TAG, "getCredentialService: returning CredentialService immediately");
                // if session state is open, call the onCredentialServiceLoaded immediately.
                callback.onCredentialServiceLoaded(csm.mCredentialService);
            } else {
                // add to list of callbacks.
                LOGD(TAG, "getCredentialService: adding callback to stack");
                csm.mCallbacks.add(callback);
            }
        } else {
            throw new IllegalArgumentException("Activity (" + activity.getTitle() + ") is not" +
                                                " associated with CredentialServiceManager");
        }
    }

    /**
     * Clients that have requested {@link CredentialService} by calling
     * {@link #getCredentialService(Activity, Callback)}, can call
     * this function to explicitly remove their callback, if it hasn't already been invoked.
     * @param activity
     * @param callback
     */
    public static void removeCallback(Activity activity, Callback callback) {
        final CredentialServiceManager csm = sInstances.get(activity);
        if (csm != null) {
            csm.mCallbacks.remove(callback);
        }
    }

    /**
     * This class should be constructed during Activity {@code onCreate()} onCredentialServiceLoaded.
     * @param activity Activity to attach to this instance.
     */
    public CredentialServiceManager(Activity activity) {
        mActivity = activity;
        mCallbacks = new ArrayList<>();
        mState = State.CLOSED;

        // lastly add this instance to the map of instances.
        sInstances.put(activity, this);
    }

    /**
     * Should be called on Activity's onCreate onCredentialServiceLoaded.
     */
    public void onCreate(Bundle savedInstanceBundle) {
        LOGD(TAG, "onCreate called");
        //TODO: decide from data stored in UserAccount to load a a CredentialService or launch LoginActivity.
        final UserAccount userAccount = UserAccount.getUserAccount(mActivity);
        if (userAccount.isUserSignedIn()) {
            switch(userAccount.getIdentityProvider()) {
                case GOOGLE_PLUS:
                    mCredentialService = new GoogleCredentialService();
                    break;
                case FACEBOOK:
                    mCredentialService = new FacebookCredentialService();
                    break;
                default:
                    throw new IllegalStateException("Unknown identity provider:" + userAccount.getIdentityProvider());
            }
            mCredentialService.setListener(this);
            mCredentialService.onCreate(mActivity);
        } else {
            openLoginFlow();
        }
    }

    /**
     * Should be called on attached Activity's onStart onCredentialServiceLoaded.
     */
    public void onStart() {
        LOGD(TAG, "onStart called");
        if (mCredentialService != null) {
            LOGD(TAG, "calling onStart on CredentialService");
            mCredentialService.onStart();
        }
    }

    /**
     * Should be called on attached Activity's onResume onCredentialServiceLoaded.
     */
    public void onResume() {
        LOGD(TAG, "onResume called");
        isResumed = true;
        // if session has been opened, but there are callbacks
        // that are not called, call them.
        callAllCallbacks();
    }

    /**
     * Should be called on attached Activity's onPause onCredentialServiceLoaded.
     */
    public void onPause() {
        LOGD(TAG, "onPause called");
        isResumed = false;
    }

    /**
     * Should be called on attached Activity's onStop onCredentialServiceLoaded.
     */
    public void onStop() {
        LOGD(TAG, "onStop called");
        if (mCredentialService != null) {
            LOGD(TAG, "calling onStop on CredentialService");
            mCredentialService.onStop();
        }
    }

    /**
     * Should be called on attached Activity's onDestroy onCredentialServiceLoaded.
     */
    public void onDestroy() {
        LOGD(TAG, "onDestroy called");
        sInstances.remove(mActivity);
        mActivity = null;
        mCallbacks = null;
        mCredentialService = null;
    }

    /**
     * Should be called on Activity's onActivityResult onCredentialServiceLoaded.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ID_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                // Authentication has completed.
                // Tries loading the CredentialService again, and calling onCreate on it.
                switch (UserAccount.getUserAccount(mActivity).getIdentityProvider()) {
                    case GOOGLE_PLUS:
                        mCredentialService = new GoogleCredentialService();
                        break;
                    case FACEBOOK:
                        mCredentialService = new FacebookCredentialService();
                        break;
                }
                mCredentialService.setListener(this);
                // Activity is going to call from onStart, not onCreate.
                // so we should call onCreate manually here.
                mCredentialService.onCreate(mActivity);
            } else {
                // If login flow is cancelled, finish attached activity.
                mActivity.finish();
            }
        }
    }

    @Override
    public void onStateChanged(CredentialService service, CredentialService.State state) {
        switch (state) {
            case TOKEN_LOADED:
                mState = State.OPEN;
                // Token has been loaded.
                if (isResumed) {
                    // if attached Activity is in resumed state,
                    // call all callbacks.
                    callAllCallbacks();
                }
                break;
            default:
                LOGE(TAG, "Loaded CredentialService (" + service.getName() + ") in state: " + state);
                service.closeAndClearTokenInformation();
                openLoginFlow();
        }
    }

    private void openLoginFlow() {
        Intent intent = new Intent(mActivity, LoginActivity.class);
        mActivity.startActivityForResult(intent, REQUEST_ID_LOGIN);
    }

    private void callAllCallbacks() {
        LOGD(TAG, "Calling all callbacks. State: " + mState + ", #callbacks: " + mCallbacks.size());
        if (mState == State.OPEN) {
            for (Callback callback : mCallbacks) {
                callback.onCredentialServiceLoaded(mCredentialService);
            }
            mCallbacks.clear();
        }
    }

    public static enum State {
        OPEN, // Session is open, and value of mCredentialService is properly set.
        CLOSED // Session is closed and requires user interaction to open it.
    }

    public interface Callback {

        /**
         * Each callback is called exactly once.
         *
         * @param credentialService
         */
        public void onCredentialServiceLoaded(CredentialService credentialService);
    }

}
