package com.oursaviorgames.android.ui;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.crashlytics.android.Crashlytics;

import com.oursaviorgames.android.R;
import io.fabric.sdk.android.Fabric;
import rx.Observable;
import rx.Subscription;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Base activity for the application.
 * <p>
 * This Activity also provides functionality for {@link rx.Observable}s
 * to bind themselves to Activity lifecycle methods, and for {@link rx.Subscription}s
 * to unsubscribe automatically when Activity is destroyed.
 */
public class BaseActivity extends ActionBarActivity {

    private static final String TAG = makeLogTag(BaseActivity.class);

    /**
     * Subject tracking this Activity's lifecycle events.
     */
    private final BehaviorSubject<LifecycleEvent> mLifecycleSubject = BehaviorSubject.create();
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGD(TAG, "Activity " + getTitle() + " created");

        // Use crashlytics for all Activities.
        Fabric.with(this, new Crashlytics());

        // Init CompositeSubscription
        mCompositeSubscription = new CompositeSubscription();
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.CREATE);

        // Maybe null if this Activity doesn't have an ActionBar.
        ActionBar actionBar = getSupportActionBar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // Removes shadow for Android version >= Lollipop.
            if (actionBar != null) {
                actionBar.setElevation(0.f);
            }

            // Sets custom 'recent apps' colors and decoration.
            final Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_moonrise_48dp);
            final int color = getResources().getColor(R.color.primaryDark);
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), icon, color));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LOGD(TAG, "Activity " + getTitle() + " started");
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LOGD(TAG, "Activity " + getTitle() + " resumed");
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.RESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LOGD(TAG, "Activity " + getTitle() + " paused");
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.PAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LOGD(TAG, "Activity " + getTitle() + " stopped");
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.STOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LOGD(TAG, "Activity " + getTitle() + " destroyed");
        // Updates Lifecycle subject.
        mLifecycleSubject.onNext(LifecycleEvent.DESTROY);
        mCompositeSubscription.unsubscribe();
    }

    /**
     * Returns an Observable of this Activity's lifecycle events.
     */
    private Observable<LifecycleEvent> getLifecycle() {
        return mLifecycleSubject.asObservable();
    }

    /**
     * Adds {@link rx.Subscription} {@code s} to this Activity's
     * {@link rx.subscriptions.CompositeSubscription}.
     */
    protected final void addSubscription(Subscription s) {
        mCompositeSubscription.add(s);
    }

    /**
     * Binds the given source to this Activity's lifecycle.
     * @param source the source sequence.
     */
    protected final <T> Observable<T> bindObservable(Observable<T> source) {
        return LifecycleObservable.bindActivityLifecycle(getLifecycle(), source);
    }

}
