package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.UserAccount;

/**
 * Hello, World!
 */
public class HelloFragment extends Fragment {

    private HelloFragmentListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment {@link HelloFragment}.
     */
    public static HelloFragment newInstance() {
        return new HelloFragment();
    }

    public HelloFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_hello_world, container, false);
        // Sets onClickListeners
        ImageButton gplusButton = (ImageButton) rootView.findViewById(R.id.gplusButton);
        ImageButton facebookButton = (ImageButton) rootView.findViewById(R.id.facebookButton);
        gplusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identityProviderClicked(UserAccount.IdentityProvider.GOOGLE_PLUS);
            }
        });
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                identityProviderClicked(UserAccount.IdentityProvider.FACEBOOK);
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (HelloFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement HelloFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /** Should be called by when one of the buttons is clicked */
    private void identityProviderClicked(UserAccount.IdentityProvider identityProvider) {
        mListener.onIdentityProviderSelected(identityProvider);
    }

    /**
     * Notifies containing Activity which IdentityProvider was selected by the user.s
     */
     public interface HelloFragmentListener {

        public void onIdentityProviderSelected(UserAccount.IdentityProvider identityProvider);
    }

}
