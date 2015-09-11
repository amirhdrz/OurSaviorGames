package com.oursaviorgames.android.backend;

import rx.Subscriber;

/**
 * Created by amir on 30/01/15.
 */
public abstract class RoboSubsciber<T> extends Subscriber<T> {

    protected RoboSubsciber() {
        super();
    }

    protected RoboSubsciber(Subscriber<?> op) {
        super(op);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {

    }
}
