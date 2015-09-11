package com.oursaviorgames.android.ui.menu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import com.oursaviorgames.android.R;

/**
 * A {@link MenuItem} that opens a dialog window.
 */
public abstract class DialogMenuItem extends MenuItem implements DialogInterface.OnDismissListener {

    private AlertDialog mDialog;

    public DialogMenuItem(Context context, int titleResId) {
        super(context, titleResId);
    }

    @Override
    protected final void onClick(View widget) {
        super.onClick(widget);
        if (mDialog != null && mDialog.isShowing()) return;

        AlertDialog.Builder builder = prepareDialog();
        if (builder ==  null) {
            throw new IllegalStateException("prepareDialog returned null");
        }
        builder.setOnDismissListener(this);
        mDialog = builder.create();
        mDialog.show();
        // Show blue text color positive button for pre-lollipop devices.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Must call show before accessing buttons.
            Button positiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (positiveButton != null) {
                positiveButton.setTextColor(getContext().getResources().getColor(R.color.blue));
            }
        }
    }

    protected abstract AlertDialog.Builder prepareDialog();

    @Override
    public void onDismiss(DialogInterface dialog) {
        mDialog = null;
    }
}
