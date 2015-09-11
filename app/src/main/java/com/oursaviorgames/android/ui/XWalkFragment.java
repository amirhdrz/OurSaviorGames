package com.oursaviorgames.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

public class XWalkFragment extends Fragment {

    public static final String ARG_URL = "url";

    public static XWalkFragment instantiate(String url) {
        if (url == null) {
            throw new IllegalArgumentException("Url must not be null");
        }
        XWalkFragment fragment = new XWalkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    public XWalkFragment() {
        // Required public constructor.
    }


    private String mUrl;
    private XWalkView mXwalkView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUrl = args.getString(ARG_URL, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mXwalkView = new XWalkView(getActivity(), getActivity());
        mXwalkView.setResourceClient(new XWalkResourceClient(mXwalkView));
        mXwalkView.setUIClient(new XWalkUIClient(mXwalkView));
        mXwalkView.load(mUrl, null);
        return mXwalkView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mXwalkView != null) {
            mXwalkView.pauseTimers();
            mXwalkView.onHide();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mXwalkView != null) {
            mXwalkView.resumeTimers();
            mXwalkView.onShow();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mXwalkView != null) {
            mXwalkView.onDestroy();
        }
    }

}
