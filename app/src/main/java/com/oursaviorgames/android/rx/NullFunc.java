package com.oursaviorgames.android.rx;

import rx.functions.Func1;

/**
 * For every item emitted, returns null instead.
 */
//TODO: there should be better ways than using this.
public class NullFunc<T> implements Func1<T, Void> {
    @Override
    public Void call(T t) {
        return null;
    }
}
