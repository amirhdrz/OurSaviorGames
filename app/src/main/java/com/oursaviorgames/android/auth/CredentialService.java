package com.oursaviorgames.android.auth;

import android.app.Activity;
import android.content.Intent;

import java.io.IOException;

import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.util.Preconditions;
import rx.Observable;
import rx.Subscriber;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * OAUTH identity provider client.
 */
//TODO: if the server indicates that the token is invalid, invalidate the cached token,
    // either on our side or the facebook or gplus client.
public abstract class CredentialService {

    private static final String TAG = makeLogTag(CredentialService.class);

    private Exception mException; // The unrecoverable exception.
    private State mState;
    private String mAccessToken;
    private StateListener mListener;

    public CredentialService () {
        mState = State.INITIALIZING;
        mAccessToken = null;
    }

    public final void setListener(StateListener listener) {
        mListener = listener;
    }

    abstract public void onCreate(Activity activity);

    abstract public void onStart();

    abstract public void onStop();

    abstract public void onActivityResult(int requestCode, int resultCode, Intent data);

    abstract public UserAccount.IdentityProvider getName();

    abstract public void requestUserInfo(UserInfoRequestCallback callback);

    /**
     * Observable should only be used on {@link rx.android.schedulers.AndroidSchedulers#mainThread()}.
     * <p>
     *     Notifies Observers of {@link GetUserInfoException} if {@link UserInfo} could not
     *     be retrieved.
     * </p>
     */
    public Observable<UserInfo> requestUserInfo() {
        return Observable.create(new Observable.OnSubscribe<UserInfo>() {
            @Override
            public void call(final Subscriber<? super UserInfo> sub) {
                requestUserInfo(new UserInfoRequestCallback() {
                    @Override
                    public void onCompleted(UserInfo userInfo) {
                        if (userInfo != null) {
                            sub.onNext(userInfo);
                            sub.onCompleted();;
                        } else {
                            sub.onError(new GetUserInfoException("Could not retrieve UserInfo"));
                        }
                    }
                });
            }
        });
    }

    /**
     * Clears session and cached token information.
     * Implementations must guarantee that the session is closed
     * after this call.
     */
    abstract protected void clearTokenInformation();

    /**
     * If CredentialService is in {@code State.RESOLVABLE_EXCEPTION},
     * this method tries to resolve it.
     * The resolution might require user interaction.
     * If exception is already resolved or is resolving, returns immediately.
     */
    //TODO: define the meaning of resolve exception more clearly.
    abstract public void resolveException();

    /**
     * Access token is cached in memory.
     * @return
     */
    public final String getAccessToken() {
        return mAccessToken;
    }

    public final State getState() {
        return mState;
    }

    public final Exception getException() {
        return mException;
    }

    public final void closeAndClearTokenInformation() {
        clearTokenInformation();
        mAccessToken = null;
        mState = State.CLOSED;
    }

    protected void setStateTokenLoaded(String accessToken) {
        LOGD(TAG, "State: TOKEN_LOADED with " + accessToken);
        mAccessToken = accessToken;
        mState = State.TOKEN_LOADED;
        if (mListener != null) {
            mListener.onStateChanged(this, mState);
        }
    }

    protected void setStateResolveableException() {
        LOGD(TAG, "State: RESOLVABLE_EXCEPTION");
        mState = State.RESOLVABLE_EXCEPTION;
        if (mListener != null) {
            mListener.onStateChanged(this, mState);
        }
    }

    protected void setStateFailed(Exception exception) {
        LOGD(TAG, "State: FAILED");
        if (exception != null && exception.getMessage() != null) LOGD(TAG, exception.getMessage());
        mException = exception;
        mState = State.FAILED;
        if (mListener != null) {
            mListener.onStateChanged(this, mState);
        }
    }

    /** Represents the state of the CredentialService */
    //TODO: the state needs to contain failure types so the listener
        // might try to resolve them based on the type of failure.
    public static enum State {
        INITIALIZING,
        TOKEN_LOADED,
        CLOSED,
        RESOLVABLE_EXCEPTION, // The exception can be resolved by the user.
        FAILED
    }

    /**
     * Listener interface to be implemented by clients who want
     * to be notified of the service state changes.
     */
    public interface StateListener {

        public void onStateChanged(CredentialService service, State state);

    }

    /**
     * Callback interface for when a client requests
     * user info through {@link #requestUserInfo(UserInfoRequestCallback)}.
     */
    public interface UserInfoRequestCallback {

        /**
         * Request to get user info completed.
         * @param userInfo Returned object. maybe null, or contain null fields.
         */
        public void onCompleted(UserInfo userInfo);

    }

    /**
     * Represents a Person's worth of information from a CredentialService.
     */
    public static class UserInfo {

        public final String name;
        public final Gender gender;
        public final String profileImageUrl;

        public UserInfo(String name, Gender gender, String profileImageUrl) {
            this.name = Preconditions.checkNotNull(name);
            this.gender = Preconditions.checkNotNull(gender);
            this.profileImageUrl = Preconditions.checkNotNull(profileImageUrl);
        }

        /**
         * This enum should match the same enum on the server.
         */
        public enum Gender {
            OTHER,
            FEMALE,
            MALE
        }
    }
}
