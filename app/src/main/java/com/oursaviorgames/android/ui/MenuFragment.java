package com.oursaviorgames.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.ui.menu.CheckBoxMenuItem;
import com.oursaviorgames.android.ui.menu.ClearGameCacheMenuItem;
import com.oursaviorgames.android.ui.menu.MenuAdapter;
import com.oursaviorgames.android.ui.menu.MenuCategory;
import com.oursaviorgames.android.ui.menu.MenuItem;
import com.oursaviorgames.android.ui.menu.SignOutMenuItem;
import com.oursaviorgames.android.util.PrefUtils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class MenuFragment extends AuthedRoboHelperFragment {

    private static final String TAG = makeLogTag(MenuFragment.class);

    public static MenuFragment createInstance() {
        return new MenuFragment();
    }

    @InjectView(R.id.listView)
    ListView mListView;

    private MenuAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MenuAdapter(getActivity(), createMenu());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.inject(this, rootView);

        mListView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private MenuCategory[] createMenu() {
        Context c = getActivity();

        MenuCategory account = new MenuCategory(c, R.string.account);
        account.addMenuItem(new MenuItem(c, R.string.edit_your_profile, new Intent(c, ProfileActivity.class)));
        account.addMenuItem(new SignOutMenuItem(getActivity()));

        MenuCategory settings = new MenuCategory(c, R.string.settings);
        settings.addMenuItem(new CheckBoxMenuItem(c, R.string.notifications_for_new_games,
                PrefUtils.PREF_NEW_GAME_NOTIFICATION, PrefUtils.PREF_NEW_GAME_NOTIFICATION_DEF));
        settings.addMenuItem(new CheckBoxMenuItem(c, R.string.loop_video_playback,
                PrefUtils.PREF_LOOP_VIDEO, PrefUtils.PREF_LOOP_VIDEO_DEF));
        settings.addMenuItem(new ClearGameCacheMenuItem(getActivity()));

        MenuCategory information = new MenuCategory(c, R.string.information);
        information.addMenuItem(new MenuItem(c, R.string.our_savior_games, new Intent(c, AboutActivity.class)));
        information.addMenuItem(new MenuItem(c, R.string.feedback_send, new Intent(c, FeedbackActivity.class)));

        return new MenuCategory[]{account, settings, information};
    }

}
