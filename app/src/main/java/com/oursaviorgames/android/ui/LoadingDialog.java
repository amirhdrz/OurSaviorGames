package com.oursaviorgames.android.ui;

import android.app.AlertDialog;
import android.content.Context;

import com.oursaviorgames.android.R;

public class LoadingDialog extends AlertDialog {

    private LoadingDialog(Context context) {
        super(context);
    }

    public static class Builder {

        final AlertDialog.Builder mBuilder;

        public Builder(Context context) {
            mBuilder = new AlertDialog.Builder(context, THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage(R.string.please_wait)
                    .setCancelable(false);
        }

        public Builder setMessage(int messageResId) {
            mBuilder.setMessage(messageResId);
            return this;
        }

        public AlertDialog build() {
            return mBuilder.create();
        }

    }


}
