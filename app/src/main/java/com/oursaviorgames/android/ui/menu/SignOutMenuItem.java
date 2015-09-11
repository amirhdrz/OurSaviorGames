package com.oursaviorgames.android.ui.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.UserAccount;

public final class SignOutMenuItem extends DialogMenuItem implements AlertDialog.OnClickListener {

    private Activity mMainActivity;

    public SignOutMenuItem(Activity mainActivity) {
        super(mainActivity, R.string.sign_out);
        mMainActivity = mainActivity;
    }

    @Override
    protected AlertDialog.Builder prepareDialog() {
        return new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setMessage(R.string.are_you_sure)
                .setNegativeButton(R.string.cancel, this)
                .setPositiveButton(R.string.sign_out, this);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (DialogInterface.BUTTON_POSITIVE == which) {
            // Positive result.
            UserAccount.getUserAccount(getContext()).clearAccountData();
            //TODO: clear saved games table and all other personal information.
            mMainActivity.recreate();
        }
    }
}
