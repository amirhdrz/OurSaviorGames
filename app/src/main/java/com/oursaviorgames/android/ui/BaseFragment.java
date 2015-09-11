package com.oursaviorgames.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rx.Observable;
import rx.Subscription;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Base Fragment for all application's fragments.
 * <p>
 * Contains methods to allow {@link Observable}s to bind themsevles
 * to this fragment's lifecycle and for {@link rx.Subscription}s to
 * be unsubscribed when during fragment's {@link #onDestroy()} callback.
 */
public class BaseFragment extends Fragment {

    protected String MY_TAG;

    private final BehaviorSubject<LifecycleEvent> mLifeCycleObject = BehaviorSubject.create();

    private CompositeSubscription mCompositeSubscription;

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onAttach(Activity activity) {
        MY_TAG = makeLogTag(this.getClass());
        LOGD(MY_TAG, "onAttach");
        super.onAttach(activity);

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.ATTACH);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOGD(MY_TAG, "onCreate");
        super.onCreate(savedInstanceState);

        mCompositeSubscription = new CompositeSubscription();
        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.CREATE);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LOGD(MY_TAG, "onCreateView");

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.CREATE_VIEW);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LOGD(MY_TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onStart() {
        LOGD(MY_TAG, "onStart");
        super.onStart();

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.START);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onResume() {
        LOGD(MY_TAG, "onResume");
        super.onResume();

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.RESUME);
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onPause() {
        LOGD(MY_TAG, "onPause");

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.PAUSE);

        super.onPause();
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onStop() {
        LOGD(MY_TAG, "onStop");

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.STOP);

        super.onStop();
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onDestroyView() {
        LOGD(MY_TAG, "onDestroyView");

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.DESTROY_VIEW);

        super.onDestroyView();
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onDestroy() {
        LOGD(MY_TAG, "onDestroy");
        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.DESTROY);
        super.onDestroy();
        mCompositeSubscription.unsubscribe();
    }

    /**
     * Subclasses should call super implementation.
     */
    @Override
    public void onDetach() {
        LOGD(MY_TAG, "onDetach");

        // Updates Lifecycle subject.
        mLifeCycleObject.onNext(LifecycleEvent.DETACH);

        super.onDetach();
    }

    /**
     * Returns an Observable of this Activity's lifecycle events.
     */
    protected Observable<LifecycleEvent> getLifecycle() {
        return mLifeCycleObject.asObservable();
    }

    /**
     * Adds {@link rx.Subscription} {@code s} to this Fragment's
     * {@link rx.subscriptions.CompositeSubscription}.
     */
    protected void addSubscription(Subscription s) {
        mCompositeSubscription.add(s);
    }

    /**
     * Binds the given source to this Fragment's lifecycle.
     * @param source the source sequence.
     */
    protected <T> Observable<T> bindObservable(Observable<T> source) {
        return LifecycleObservable.bindFragmentLifecycle(getLifecycle(), source);
    }



}

