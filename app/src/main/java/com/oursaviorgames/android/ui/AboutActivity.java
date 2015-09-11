package com.oursaviorgames.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.ui.widget.RoboTextView;
import com.oursaviorgames.android.util.Utils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class AboutActivity extends BaseActivity {

    private static final String TAG = makeLogTag(AboutActivity.class);

    private static final String ROOT_URL = "http://oursaviorgames.com/";
    private static final String TOS_URL = ROOT_URL + "tos.html";
    private static final String PRIVACY_POLICY_URL = ROOT_URL + "privacy.html";
    private static final String LICENCES_URL = ROOT_URL + "licenses.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new AboutFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            boolean xwalkPopped = getSupportFragmentManager().popBackStackImmediate("xwalk", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (xwalkPopped) return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchWebView(String url) {
        Fragment xwalkFragment = XWalkFragment.instantiate(url);

        // replace container with Xwalk fragment.
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, xwalkFragment)
                .addToBackStack("xwalk")
                .commit();
    }

    /**
     * About fragment.
     */
    public static class AboutFragment extends Fragment {

        @InjectView(R.id.version)
        RoboTextView mVersion;

        public AboutFragment() {
            // Required public constructor.
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_about, container, false);
            ButterKnife.inject(this, rootView);

            String versionName = Utils.getVersionName(getActivity());
            if (versionName != null) {
                mVersion.setText("VERSION " + versionName);
            }

            return rootView;
        }

        @OnClick(R.id.tosButton)
        public void onTOSClicked() {
            openUrl(TOS_URL);
        }

        @OnClick(R.id.privacyPolicyButton)
        public void onPrivacyPolicyClicked() {
            openUrl(PRIVACY_POLICY_URL);
        }

        @OnClick(R.id.licencesButton)
        public void onLicensesClicked() {
            openUrl(LICENCES_URL);
        }

        private void openUrl(String url) {
            ((AboutActivity) getActivity()).launchWebView(url);
        }

    }

}
