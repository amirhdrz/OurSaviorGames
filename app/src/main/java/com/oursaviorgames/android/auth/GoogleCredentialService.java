package com.oursaviorgames.android.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.IOException;
import java.lang.ref.WeakReference;

import com.oursaviorgames.android.data.UserAccount;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class GoogleCredentialService extends CredentialService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = makeLogTag(GoogleCredentialService.class);

    private static final String SCOPES = "https://www.googleapis.com/auth/plus.login";
    private static final int RC_SIGN_IN = 0xbeeb;

    private Activity mActivity;
    private GoogleApiClient mApiClient;
    private ConnectionResult mConnectionResult;
    private boolean mIntentInProgress = false;
    private boolean mSignInClicked = false;

    @Override
    public void onCreate(Activity activity) {
        mActivity = activity;
        mApiClient = new GoogleApiClient.Builder(activity, this, this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    @Override
    public void onStart() {
        mApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mApiClient.isConnected()) {
            mApiClient.disconnect();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mApiClient.isConnecting()) {
                mApiClient.connect();
            }
        }
    }

    @Override
    public  void resolveException() {
        if (mApiClient.isConnecting() || mApiClient.isConnected()) {
            // Connecting to service or already connected.
            // Nothing to resolve.
            return;
        }
        mSignInClicked = true;
        resolveSignInError();
    }

    @Override
    public UserAccount.IdentityProvider getName() {
        return UserAccount.IdentityProvider.GOOGLE_PLUS;
    }

    @Override
    public void requestUserInfo(UserInfoRequestCallback callback) {
        if (mApiClient != null && mApiClient.isConnected()) {
            Person person = Plus.PeopleApi.getCurrentPerson(mApiClient);
            UserInfo.Gender gender = UserInfo.Gender.OTHER;
            if (person.hasGender()) {
                switch (person.getGender()) {
                    case Person.Gender.MALE:
                        gender = UserInfo.Gender.MALE;
                        break;
                    case Person.Gender.FEMALE:
                        gender = UserInfo.Gender.FEMALE;
                        break;
                    case Person.Gender.OTHER:
                        gender = UserInfo.Gender.OTHER;
                        break;
                }
            }
            String imageUrl = null;
            if (person.hasImage()) {
                imageUrl = person.getImage().getUrl();
            }

            // Sets name to user's nickname if it has one,
            // else sets name to user's given name + family name.
            String name = null;
            if (person.hasNickname()) {
                name = person.getNickname();
            } else if (person.hasName()) {
                Person.Name gName = person.getName();
                name = "";
                if (gName.hasGivenName()) {
                    name += gName.getGivenName();
                }
                if (gName.hasFamilyName()) {
                    name += gName.getFamilyName();
                }
            }

            if (name != null && imageUrl != null) {
                callback.onCompleted(new UserInfo(name, gender, imageUrl));
            } else {
                callback.onCompleted(null);
            }
        } else {
            callback.onCompleted(null);
        }
    }

    @Override
    protected void clearTokenInformation() {
        if (getAccessToken() != null) {
            try {
                LOGD(TAG, "Clearing GoogleAuthUtil cached token");
                GoogleAuthUtil.clearToken(mActivity, getAccessToken());
            } catch (Exception e) {
                // nobody really cares.
                e.printStackTrace();
            }
        }
        if (mApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mApiClient);
            mApiClient.disconnect();
        }
    }

    /** GoogleApiClient onCredentialServiceLoaded */
    @Override
    public void onConnected(Bundle bundle) {
        // Google authentication is successful.
        // Kick off a thread in the background to get an access token.
        mSignInClicked = false;
        new GetTokenAsyncTask(mActivity, Plus.AccountApi.getAccountName(mApiClient)).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        //TODO: set state to token loaded.
    }

    /** GoogleApiClient onCredentialServiceLoaded */
    @Override
    public void onConnectionSuspended(int i) {
        LOGD(TAG, "onConnectionSuspended with id(" + i + ")");
        //TODO: do something here.
    }

    /** GoogleApiClient onCredentialServiceLoaded */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = connectionResult;
            if (connectionResult.hasResolution()) {
                // ConnectionResult has a resolution.
                setStateResolveableException();
            } else {
                // ConnectionResult has no resolution..
                // Set state to Failed.
                setStateFailed(new Exception("ConnectionResult has no resolution. Error code (" +
                connectionResult.getErrorCode() + ")"));
            }

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mActivity.startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mApiClient.connect();
            }
        }
    }

    /** Callback from {@code GetTokenAsyncTask}. */
    public void getAccessTokenCallback(String accessToken, Exception exception) {
        if (exception != null) LOGE(TAG, exception.getMessage());
        if (accessToken != null) {
            setStateTokenLoaded(accessToken);
        } else {
            setStateFailed(exception);
        }
    }

    /** Gets an access token using GoogleAuthUtil */
    private class GetTokenAsyncTask extends AsyncTask<Void, Void, String> {

        private final WeakReference<Context> mContext;
        private final String mAccountName;

        private Exception mException;

        public GetTokenAsyncTask(Context context, String accountName) {
            mContext = new WeakReference<>(context);
            mAccountName = accountName;
        }

        @Override
        protected String doInBackground(Void... params) {
            String accessToken = null;
            if (mContext.get() != null) {
                try {
                    accessToken = GoogleAuthUtil.getToken(mContext.get(), mAccountName,
                            "oauth2:" + SCOPES);
                    //TODO: double check the access token. It maybe a bad token.
                } catch (UserRecoverableAuthException e) {
                    // Did not expect to get here.
                    mException = e;
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                    mException = e;
                } catch (IOException e) {
                    // network error, try later.
                    // retry with exponential backoff algorithm.
                    mException = e;
                    e.printStackTrace();
                }

            }
            return accessToken;
        }

        @Override
        protected void onPostExecute(String s) {
            if (mContext.get() != null) {
                getAccessTokenCallback(s, mException);
            }
        }
    }

}
