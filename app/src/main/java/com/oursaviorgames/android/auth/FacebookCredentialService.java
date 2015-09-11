package com.oursaviorgames.android.auth;

import android.app.Activity;
import android.content.Intent;

import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import com.oursaviorgames.android.data.UserAccount;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class FacebookCredentialService extends CredentialService implements Session.StatusCallback {

    private static final String TAG = makeLogTag(FacebookCredentialService.class);

    private Activity mActivity;

    @Override
    public void onCreate(Activity activity) {
        mActivity = activity;
        // Opens session without requiring user interaction.
        Session session = Session.openActiveSession(mActivity, false, this);
        if (session == null) {
            // Need to try again with user interaction.
            setStateResolveableException();
        }
    }

    @Override
    public void resolveException() {
        LOGD(TAG, "Resolving facebook session exception");
        if (Session.getActiveSession() != null &&
                (Session.getActiveSession().isOpened() || Session.getActiveSession().getState() == SessionState.OPENING)) {
            // Session is already or in run of opening. Nothing to resolve.
            return;
        }
        // No cached token is available,
        // therefore ask user to login.
        Session.openActiveSession(mActivity, true, this);
    }

    @Override
    public void onStart() {
        // Do nothing.
    }

    @Override
    public void onStop() {
        // Do nothing.
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().onActivityResult(mActivity, requestCode, resultCode, data);
        }
    }

    @Override
    public UserAccount.IdentityProvider getName() {
        return UserAccount.IdentityProvider.FACEBOOK;
    }

    @Override
    public void requestUserInfo(final UserInfoRequestCallback callback) {
        if (Session.getActiveSession() != null && Session.getActiveSession().isOpened() &&
                !Session.getActiveSession().isClosed()) {
            Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (response.getError() == null) {
                        String name = user.getName();
                        UserInfo.Gender gender = UserInfo.Gender.OTHER;
                        String imageUrl = "https://graph.facebook.com/" + user.getId() + "/picture?type=large";
                        if (name != null && user.getId() != null){
                            UserInfo userInfo = new UserInfo(name, gender, imageUrl);
                            callback.onCompleted(userInfo);
                        } else {
                            callback.onCompleted(null);
                        }
                    } else {
                        LOGD(TAG, "Facebook request error: " + response.getError().toString());
                        callback.onCompleted(null);
                    }
                }
            }).executeAsync();
        }
    }

    @Override
    public void clearTokenInformation() {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
    }

    /** Callback from facebook session */
    @Override
    public void call(Session session, SessionState state, Exception exception) {
        if (exception == null) {
            if (state.isOpened() && !state.isClosed()) {
                // If access token is null, try resolving exception again.
                if (session.getAccessToken() != null && !session.getAccessToken().equals("")) {
                    setStateTokenLoaded(session.getAccessToken());
                } else {
                    resolveException();
                }
            }
        } else {
            LOGD(TAG, state.toString());
            // Checks if the operation was cancelled by the user or not.
            if (exception instanceof FacebookOperationCanceledException) {
                setStateResolveableException();
            } else {
                setStateFailed(exception);
            }
        }
    }
}
