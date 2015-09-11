package com.oursaviorgames.android.rx;

import com.crashlytics.android.Crashlytics;
import com.oursaviorgames.android.util.LogUtils;
import rx.Subscriber;

import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public abstract class AndroidSubscriber<T> extends Subscriber<T> {

    private static final String TAG = makeLogTag(AndroidSubscriber.class);

    /**
     * Called when the Observable stream has finished.
     * As opposed to {@link rx.Subscriber#onCompleted()}, this method
     * is called whether or not an error has occurred.
     * This is similar to Java try-catch 'finally' block.
     */
    @Deprecated
    public void onFinished() {

    }

    /**
     * If Observable stream throws an exception,
     * this method is called before call to {@link #onFinished()}.
     * @param e
     * @return True if the Throwable is handled, false otherwise.
     */
    public boolean onError2(Throwable e) {
        return false;
    }


    @Override
    public abstract  void onNext(T t);

    @Override
    public final void onCompleted() {
        onFinished();
    }

    @Override
    public final void onError(Throwable e) {
        if (LogUtils.DEBUG) {
            LOGE(TAG, "AndroidSubscriber error");
            e.printStackTrace();
        }
        onError2(e);
        if (!onError2(e)) {
            Crashlytics.logException(e);
        }
        onFinished();
    }

}
