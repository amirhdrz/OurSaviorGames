package com.oursaviorgames.android.ui;

import android.os.Bundle;

import com.oursaviorgames.android.R;

public class ProfileActivity extends AuthedActivity implements ProfileFragment.UserSignUpFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new ProfileFragment())
                    .commit();
        }
    }

    @Override
    public void onUserSignUpFragmentInteraction(int interaction) {
        if (ProfileFragment.DONE == interaction
                || ProfileFragment.CANCELLED == interaction) {
            finish();
        }
    }
}
