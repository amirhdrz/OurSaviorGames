package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.backend.processor.ConflictException;
import com.oursaviorgames.android.backend.processor.ForbiddenException;
import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.widget.ProfilePictureView;
import com.oursaviorgames.android.ui.widget.ProgressBarSwitcher;
import com.oursaviorgames.android.ui.widget.UsernameEditText;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.LogUtils;
import com.oursaviorgames.android.util.TypefaceUtils;
import com.oursaviorgames.android.util.UiUtils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class ProfileFragment extends AuthedRoboHelperFragment {

    private static final String TAG = makeLogTag(ProfileFragment.class);

    // User interaction codes
    /** Indicates the user has signed up successfully */
    public static final int CANCELLED = 0;
    public static final int DONE      = 1;

    // startActivityForResult request codes.
    private static final int CROPPER_RQST_CODE = 1;

    @InjectView(R.id.title)
    TextView mTitleTextView;
    @InjectView(R.id.username)
    UsernameEditText    mUsernameEditText;
    @InjectView(R.id.profilePictureView)
    ProfilePictureView  mProfilePictureView;
    @InjectView(R.id.okButtonSwitcher)
    ProgressBarSwitcher mOkButtonSwitcher;
    @InjectView(R.id.okButton)
    Button              mOkButton;

    private UserSignUpFragmentListener mListener;

    public ProfileFragment() {
        // Required public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.layout_profile, container, false);
        ButterKnife.inject(this, rootView);

        TypefaceUtils.applyTypeface(mOkButton, TypefaceUtils.ROBOTO_REGULAR);
        TypefaceUtils.applyTypeface(mTitleTextView, TypefaceUtils.OCR_B);

        final UserAccount userAccount = UserAccount.getUserAccount(getActivity());
        mUsernameEditText.setText(userAccount.getUsername());

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (UserSignUpFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CROPPER_RQST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                LOGD(TAG, "Cropped image activity result received: " + data.getData().toString());
                mProfilePictureView.setProfileImageUri(data.getData());
            }
        }
    }

    /**
     * Called when one of the profile picture choosers is clicked.
     * @param view
     */
    @OnClick({R.id.profilePictureView, R.id.profilePictureButton})
    @SuppressWarnings("unused")
    public void onProfilePictureClicked(View view) {
        Intent intent = new Intent(getActivity(), CropperActivity.class);
        startActivityForResult(intent, CROPPER_RQST_CODE);
    }

    /**
     * Called when 'OK READY' button is clicked.
     * @param view
     */
    @OnClick(R.id.okButton)
    @SuppressWarnings("unused")
    public void onOkButtonClicked(View view) {
        // Gets validated username.
        final String username = mUsernameEditText.getValidatedString();
        final String oldUsername = UserAccount.getUserAccount(getActivity()).getUsername();
        if (username == null) {
            // If username could not be validated or it is same as old username.
            return;
        }
        // Saves profile picture.
        try {
            boolean profileChanged = mProfilePictureView.saveProfilePicture();
            if (profileChanged) {
                getRoboHelper().subscribe(new Action1<RoboHelper>() {
                    @Override
                    public void call(RoboHelper roboHelper) {
                        roboHelper.uploadProfilePicture();
                    }
                });
            }
        } catch (IOException e) {
            ErrorUtils.showTopError(getActivity(), R.string.error_save_profile_pic);
            return;
        }

        // If username hasn't changed, return immediately.
        if (oldUsername != null && username.equals(oldUsername)) {
            mListener.onUserSignUpFragmentInteraction(CANCELLED);
            return;
        }
        // Start showing the loading spinner.
        mOkButtonSwitcher.showSpinner();
        addSubscription(bindObservable(getRoboHelper()
                        .flatMap(new Func1<RoboHelper, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(RoboHelper roboHelper) {
                                return roboHelper.updateUserProfile(username);
                            }
                        }))
                        .subscribe(new AndroidSubscriber<Void>() {
                            @Override
                            public void onFinished() {
                                //Hide the spinner.
                                mOkButtonSwitcher.hideSpinner();
                            }

                            @Override
                            public boolean onError2(Throwable e) {
                                LOGD(TAG, "usersignup error onCredentialServiceLoaded");
                                if (LogUtils.DEBUG) {
                                    e.printStackTrace();
                                }
                                boolean handled = false;
                                if (e instanceof ConflictException) {
                                    // Shows error message.
                                    String msg = TextUtils.join(" ",
                                            new String[]{getString(R.string.v_the_user_name),
                                                    username,
                                                    getString(R.string.v_is_not_available)});
                                    ErrorUtils.showTopError(getActivity(), msg);
                                    handled = true;
                                } else if (e instanceof ForbiddenException) {
                                    ErrorUtils.showTopError(getActivity(),
                                            "Woops, you're already registered." +
                                                    "You should not be on this page.");
                                    handled = true;
                                }
                                return handled;
                            }

                            @Override
                            public void onNext(Void aVoid) {
                                LOGD(TAG, "User is now registered on the server");
                                mListener.onUserSignUpFragmentInteraction(DONE);
                            }
                        })
        );
    }

    /**
     * This interface must be implemented by the activity
     * that contains this fragment.
     */
    public interface UserSignUpFragmentListener {

        public void onUserSignUpFragmentInteraction(int interaction);
    }

}
