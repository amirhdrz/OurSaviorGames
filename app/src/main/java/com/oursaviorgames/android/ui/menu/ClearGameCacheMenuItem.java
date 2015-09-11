package com.oursaviorgames.android.ui.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.xwalk.core.XWalkView;

import com.oursaviorgames.android.R;

public final class ClearGameCacheMenuItem extends DialogMenuItem implements AlertDialog.OnClickListener {

    private Activity mMainActivity;

    public ClearGameCacheMenuItem(Activity mainActivity) {
        super(mainActivity, R.string.clear_game_cache);
        mMainActivity = mainActivity;
    }

    @Override
    protected AlertDialog.Builder prepareDialog() {
        return new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setMessage(R.string.clear_game_cache_message)
                .setNegativeButton(R.string.cancel, this)
                .setPositiveButton(R.string.clear_cache, this);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (DialogInterface.BUTTON_POSITIVE == which) {
            //TODO: show a pleas wait dialog.
            XWalkView xwalk = new XWalkView(mMainActivity, mMainActivity);
            try {
                xwalk.clearCache(true);
            } catch (Exception e) {
                String msg = (e.getMessage() == null) ? "" : e.getMessage();
                Crashlytics.log(Log.INFO, "clear_cache_failed", msg);
            }
        }
    }
}
