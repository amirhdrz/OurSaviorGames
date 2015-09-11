package com.oursaviorgames.android.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.util.PrefUtils;
import rx.Observable;
import rx.functions.Func1;

public class FeedbackActivity extends AuthedActivity {

    private static final String FEEDBACK_TAG = "feedback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new FeedbackFragment(), FEEDBACK_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_submit_feedback) {
            ((FeedbackFragment) getSupportFragmentManager().findFragmentByTag(FEEDBACK_TAG))
                    .onSendFeedback();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ((FeedbackFragment) getSupportFragmentManager().findFragmentByTag(FEEDBACK_TAG))
                .onActivityBackPressed();
        super.onBackPressed();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FeedbackFragment extends AuthedRoboHelperFragment {

        @InjectView(R.id.feedback_input_email)
        EditText mEmailEditText;
        @InjectView(R.id.feedback_input_msg)
        EditText mMessageEditText;

        public FeedbackFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);
            ButterKnife.inject(this, rootView);

            Context context = getActivity();
            String oldEmail = PrefUtils.getSharedPrefs(context)
                    .getString(PrefUtils.PREF_FEEDBACK_SAVED_EMAIL,
                            PrefUtils.PREF_FEEDBACK_SAVED_EMAIL_DEF);
            String oldMsg = PrefUtils.getSharedPrefs(context)
                    .getString(PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE,
                            PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE_DEF);

            if (oldEmail != null) {
                mEmailEditText.setText(oldEmail);
            }
            if (oldMsg != null) {
                mMessageEditText.setText(oldMsg);
            }
            // EditText view is already populated,
            // clear the preferences.
            PrefUtils.getEditor(context)
                    .putString(PrefUtils.PREF_FEEDBACK_SAVED_EMAIL,
                            PrefUtils.PREF_FEEDBACK_SAVED_EMAIL_DEF)
                    .putString(PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE,
                            PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE_DEF)
                    .apply();

            return rootView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.reset(this);
        }

        public void onActivityBackPressed() {
            // Persists the feedback
            PrefUtils.getEditor(getActivity())
                    .putString(PrefUtils.PREF_FEEDBACK_SAVED_EMAIL, mEmailEditText.getText().toString())
                    .putString(PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE, mMessageEditText.getText().toString())
                    .commit();
        }

        public void onSendFeedback() {
            final String email = mEmailEditText.getText().toString();
            final String message = mMessageEditText.getText().toString();

            // Close activity only if something has been written
            if (!TextUtils.isEmpty(message)) {
                getRoboHelper()
                        .flatMap(new Func1<RoboHelper, Observable<Void>>() {
                            @Override
                            public Observable<Void> call(RoboHelper roboHelper) {
                                return roboHelper.sendFeedback(email, message);
                            }
                        })
                        .subscribe(new AndroidSubscriber<Void>() {
                            @Override
                            public void onNext(Void aVoid) {

                            }
                        });

                // Ends FeedbackActivity
                getActivity().finish();
            }
        }

    }

}
