package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;
import android.widget.Toast;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;

import java.io.IOException;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.util.PrefUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class FeedbackProcessor extends EndpointProcessor<Void> {

    private static final String TAG = makeLogTag(FeedbackProcessor.class);

    public static final String PARAM_EMAIL = "email";
    public static final String PARAM_MESSAGE = "message";

    private final String email;
    private final String message;

    public FeedbackProcessor(Bundle reqParams) {
        super(reqParams);
        email = reqParams.getString(PARAM_EMAIL);
        message = reqParams.getString(PARAM_MESSAGE);
    }

    @Override
    protected MobileApiEndpointRequest processRequest(HandlerService context, MobileApiEndpoint endpoint, Bundle reqParams) throws IOException {
        MobileApiEndpointRequest request = endpoint.feedback().send(message);
        if (email != null) {
            request.put("email", email);
        }
        request.execute();
        return request;
    }

    @Override
    protected void onSuccess(final HandlerService context) {
        LOGD(TAG, "onSuccess");
        context.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        context.getString(R.string.feedback_thanks),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

    @Override
    protected void onFailure(final HandlerService context, int resultCode) {
        // Persists the feedback
        PrefUtils.getSharedPrefs(context).edit()
                .putString(PrefUtils.PREF_FEEDBACK_SAVED_EMAIL, email)
                .putString(PrefUtils.PREF_FEEDBACK_SAVED_MESSAGE, message)
                .commit();

        context.getMainHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,
                        context.getString(R.string.feedback_submit_failed),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    protected Void onGetResult() {
        return null;
    }
}
