package com.oursaviorgames.android.sync;

import android.content.Context;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;

/**
 * A SyncAdapter acts as the bridge between the data stored locally,
 * and data stored on the backend server.
 * SyncAdapters create request objects which are then executed,
 * and the response is returned to the adapter.
 */
@Deprecated
public abstract class SyncAdapter {

    private Context mContext;

    /**
     * Context is set before any other calls to the SyncAdapter.
     * @param context
     */
    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * App context.
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Whether this sync adapter needs syncing.
     * This method is guaranteed to be called before {@code SyncAdapter#getRequest()}.
     * If false is returned {@link SyncAdapter#getRequest()} is not called.
     * @return
     */
    abstract boolean isSyncNeeded();

    /**
     * Returns the request object for the SyncAdapter's request.
     * So far getRequest is only called once.
     * This method should be idempotent.
     * @return MobileBackendRequest or null if a requested could not be generated.
     */
    abstract MobileApiEndpointRequest getRequest();

    /**
     * Called when SyncAdapter's request has been executed.
     * @param statusCode Response HTTP status code.
     */
    abstract void onResponse(int statusCode);

}
