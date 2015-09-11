package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import rx.functions.Func1;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.backend.processor.ConflictException;
import com.oursaviorgames.android.backend.processor.ForbiddenException;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.ui.widget.ProfilePictureView;
import com.oursaviorgames.android.ui.widget.ProgressBarSwitcher;
import com.oursaviorgames.android.ui.widget.UsernameEditText;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.LogUtils;
import com.oursaviorgames.android.util.TypefaceUtils;
import com.oursaviorgames.android.util.UiUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class UserSignupFragment extends BaseRoboFragment {

    private static final String TAG = makeLogTag(UserSignupFragment.class);

    // startActivityForResult request codes.
    private static final int CROPPER_RQST_CODE = 1;

    // Argument bundle keys.
    public static final String ARG_NAME      = "name";
    public static final String ARG_USERNAME  = "username";
    public static final String ARG_GENDER    = "gender";
    public static final String ARG_IMAGE_URI = "imageUri";

    // Injecting Views.
    @InjectView(R.id.title)
    TextView            mTitleTextView;
    @InjectView(R.id.username)
    UsernameEditText    mUsernameEditText;
    @InjectView(R.id.profilePictureView)
    ProfilePictureView  mProfilePictureView;
    @InjectView(R.id.okButtonSwitcher)
    ProgressBarSwitcher mOkButtonSwitcher;
    @InjectView(R.id.okButton)
    Button              mOkButton;

    // Member values
    private String                            mName;
    private String                            mGeneratedUsername;
    private CredentialService.UserInfo.Gender mGender;
    private Uri                               mImageUri;
    private UserSignUpFragmentListener        mListener;

    public static UserSignupFragment newInstance(String name,
                                                 String generatedUsername,
                                                 CredentialService.UserInfo.Gender gender,
                                                 Uri imageUri) {
        UserSignupFragment fragment = new UserSignupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_USERNAME, generatedUsername);
        args.putString(ARG_GENDER, gender.name());
        args.putString(ARG_IMAGE_URI, imageUri.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public UserSignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString(ARG_NAME);
            mGeneratedUsername = getArguments().getString(ARG_USERNAME);
            mGender = CredentialService.UserInfo.Gender.valueOf(getArguments().getString(ARG_GENDER));
            mImageUri = Uri.parse(getArguments().getString(ARG_IMAGE_URI));
        }
        LOGD(TAG, "UserSignUpFragment onCreate() called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.layout_profile, container, false);
        ButterKnife.inject(this, rootView);

        TypefaceUtils.applyTypeface(mOkButton, TypefaceUtils.ROBOTO_REGULAR);
        TypefaceUtils.applyTypeface(mTitleTextView, TypefaceUtils.OCR_B);

        mUsernameEditText.setText(mGeneratedUsername);
        mUsernameEditText.setSelection(mUsernameEditText.length());
        mProfilePictureView.setProfileImageUri(mImageUri);
        // ButterKnife view injection.
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CROPPER_RQST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                LOGD(TAG, "Cropped image activity result received: " + data.getData().toString());
                mProfilePictureView.setProfileImageUri(data.getData());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Sets the views to null.
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
     * Called when 'OK' button is clicked.
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.okButton)
    public void onOkButtonClicked(View view) {
        // Get the RoboHelper!
        final RoboHelper roboHelper = new RoboHelper(getRoboService2(), getActiveCredentialService());
        // Gets validated username.
        final String username = mUsernameEditText.getValidatedString();
        if (username != null) {
            // Saves profile picture.
            try {
                mProfilePictureView.saveProfilePicture();
            } catch (IOException e) {
                ErrorUtils.showTopError(getActivity(), R.string.error_save_profile_pic);
                return;
            }
            // Start showing the loading spinner.
            mOkButtonSwitcher.showSpinner();
            addSubscription(
                    bindObservable(roboHelper.signupUser(mName, username, mGender.name())
                            .map(new Func1<Void, Void>() {
                                @Override
                                public Void call(Void aVoid) {
                                    // Uploads profile picture.
                                    roboHelper.uploadProfilePicture();
                                    return null;
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
                                    mListener.onUserSignUpFragmentInteraction
                                            (UserSignUpFragmentListener.USER_SIGNED_UP);
                                }
                            })
            );
        }
    }

    /**
     * This interface must be implemented by the activity
     * that contains this fragment.
     */
    public interface UserSignUpFragmentListener {

        /** Indicates the user has signed up successfully */
        public static final int USER_SIGNED_UP = 1;

        public void onUserSignUpFragmentInteraction(int interaction);
    }

}
