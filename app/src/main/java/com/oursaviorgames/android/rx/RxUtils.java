package com.oursaviorgames.android.rx;

import java.util.Arrays;
import java.util.List;

import rx.Observable;

public class RxUtils {

    public static List<Observable<?>> combine(Observable<?>... obs) {
        return Arrays.asList(obs);
    }

}
