package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A fragment that has additional lifecycle callback methods .../TODO: finish this
 */
public class ViewPagerFragment extends BaseFragment {

    private boolean mVisible = false;
    private VisibilityLifecycleHandler mVisiblityHandler;
    private Message mVisibilityPendingMessage;

    /**
     * Returns true if the fragment is currently visible to the user.
     * This function differs from {@link #isVisible()} in that, it doesn't
     * return true unless the whole fragment is visible on the screen.
     */
    public boolean isUserVisible() {
        return mVisible;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mVisiblityHandler = new VisibilityLifecycleHandler(this);
    }

    /**
     * Subclasses must call super implementation first.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mVisibilityPendingMessage != null) {
            Message msg = Message.obtain(mVisiblityHandler);
            msg.what = mVisibilityPendingMessage.what;
            msg.sendToTarget();
            mVisibilityPendingMessage = null;
        }
    }

    /**
     * Called when the whole fragment is visible to the user,
     * but sliding animation may have not finished yet,
     * but is about to finish soon.
     * {@link #isUserVisible()} is true during this call.
     */
    protected void onVisible() {

    }

    /**
     * Called when this fragment is no longer visible to user.
     * Note {@link #isVisible()} will return true, even though,
     * the fragment may not be visible to the user.
     * {@link #isUserVisible()} is false during this call.
     */
    protected void onHidden() {

    }

    /**
     * Subclasses must call super implementation first.
     */
    @Override
    public void onPause() {
        if (mVisible) {
            mVisible = false;
            // We're going to paused state, so
            // remove any possible pending message
            // trying to change fragment's visibility state.
            mVisiblityHandler.removeAllMessages();
            onHidden();
        }
        super.onPause();
    }

    /**
     * Calls {@link #onVisible()} or {@link #onHidden()},
     * at some point in the future, but after Fragment's {@link #onResume()}
     * is called, but before {@link #onPause()} is called.
     * @param visible
     */
    public void performVisibilityChanged(boolean visible) {
        if (visible != mVisible) {
            Message msg = Message.obtain(mVisiblityHandler);
            if (visible) {
                msg.what = VisibilityLifecycleHandler.VISIBLE;
            } else {
                msg.what = VisibilityLifecycleHandler.HIDDEN;
            }
            if (isResumed()) {
                msg.sendToTarget();
            } else {
                mVisibilityPendingMessage = msg;
            }
        }
    }

    /**
     * Fragment's visibility lifecycle Handler.
     */
    private static class VisibilityLifecycleHandler extends Handler {

        private static final String TAG = makeLogTag(VisibilityLifecycleHandler.class);

        public static final int VISIBLE = 1;
        public static final int HIDDEN  = 2;

        private WeakReference<ViewPagerFragment> mFragmentRef;

        public VisibilityLifecycleHandler(ViewPagerFragment fragment) {
            mFragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final ViewPagerFragment fragment = mFragmentRef.get();
            if (fragment != null && fragment.isResumed()) {
                if (VISIBLE == msg.what) {
                    fragment.mVisible = true;
                    fragment.onVisible();
                } else if (HIDDEN == msg.what) {
                    fragment.mVisible = false;
                    fragment.onHidden();
                } else {
                    throw new IllegalStateException("Invalid Message.what: " + msg.what);
                }
            }
        }

        /**
         * Removes all Messages tagged as {@link VisibilityLifecycleHandler#VISIBLE}
         * or {@link VisibilityLifecycleHandler#HIDDEN} from message queue.
         */
        public void removeAllMessages() {
            removeMessages(VisibilityLifecycleHandler.VISIBLE);
            removeMessages(VisibilityLifecycleHandler.HIDDEN);
        }

    }

}
