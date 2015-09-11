package com.oursaviorgames.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.Toast;

import com.oursaviorgames.android.R;

public class ErrorUtils {

    /**
     * Error notifications are shown with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param stringResId Error message String resource id.
     */
    public static void showError(Context context, int stringResId) {
        showError(context, context.getString(stringResId));
    }

    /**
     * Error notifications are shown with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param msg Error message.
     */
    public static void showError(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Error notification shown on top of the screen with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param stringResId Error message String resource id.
     */
    public static void showTopError(Context context, int stringResId) {
        showTopError(context, context.getString(stringResId));
    }

    /**
     * Error notification shown on top of the screen with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param msg Error message.
     */
    public static void showTopError(Context context, String msg) {
        showTopError(context, msg, Toast.LENGTH_LONG);
    }

    /**
     * Error notification shown on top of the screen with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param stringResId Error message String resource id.
     */
    public static void showShortTopError(Context context, int stringResId) {
        showTopError(context, context.getString(stringResId), Toast.LENGTH_SHORT);
    }

    /**
     * Error notification shown on top of the screen with long duration {@link Toast#LENGTH_LONG}.
     * @param context
     * @param msg Error message.
     */
    public static void showTopError(Context context, String msg, int duration) {
        Toast toast = Toast.makeText(context, msg, duration);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public static void showNetworkError(Context context) {
        ErrorUtils.showError(context, R.string.error_io);
    }

    public static void showOfflineError(Context context) {
        ErrorUtils.showError(context, R.string.offline_not_available);
    }

    /**
     * Shows AlertDialog with message msgResId, that cannot be cancelled,
     * <p>
     * When done finishes activity by calling {@link android.app.Activity#finish()}.
     * @param activity Activity where the fatal error happened.
     * @param msgResId Message to show.
     */
    public static void showFatalErrorDialog(final Activity activity, int msgResId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.error));
        builder.setMessage(msgResId);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.show().setCanceledOnTouchOutside(false);
    }

}
