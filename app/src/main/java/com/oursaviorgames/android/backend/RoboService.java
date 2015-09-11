package com.oursaviorgames.android.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.oursaviorgames.android.backend.processor.AbstractProcessor;
import com.oursaviorgames.android.backend.processor.Processor;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.util.LogUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A bounded {@link Service} that executes {@link Processor}s and
 * returns {@link rx.Observable}s for client to subscribe to the result.
 */

/*
       Problems with current implementation:
            - requests that have failed are not retired, (either due to IOException)
                or other potentially recoverable exceptions.
            - Need a way to keep track of all observables.
            - Need a specialised RoboSubscriber that can deal with all kinds of results returned
                by Processor.
 */

public class RoboService extends HandlerService {

    private static final String TAG = makeLogTag(RoboService.class);

    /*
        Intent extra params.
     */
    public static final String EXTRA_PROCESSOR = "processor";
    public static final String EXTRA_REQ_PARAMS = "reqParams";

    /**
     * Starts {@link RoboService} as a started Service.
     * @param context Application Context.
     * @param processorClass Processor to run.
     * @param requestParams Request params for the processor.
     */
    public static void startProcessor(Context context, Class<? extends AbstractProcessor> processorClass, Bundle requestParams) {
        LOGD(TAG, "startProcessor:: processor: " + processorClass.getSimpleName() + ", requestParams: " + requestParams);
        Intent intent = new Intent(context, RoboService.class);
        intent.putExtra(EXTRA_PROCESSOR, processorClass.getName());
        intent.putExtra(EXTRA_REQ_PARAMS, requestParams);
        context.startService(intent);
    }

    // Binder returned to clients.
    private Map<Integer, Observable> mStartedObservables = new ConcurrentHashMap<>();
    private final IBinder mBinder = new RoboticBinder();
    private Handler mRoboHandler;

    @Override
    public void onCreate() {
        mRoboHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, final int startId) {
        if (intent == null) {
            throw new IllegalArgumentException("Null intent");
        }

        final String processorName = intent.getStringExtra(EXTRA_PROCESSOR);
        final Bundle reqParams = intent.getBundleExtra(EXTRA_REQ_PARAMS);

        LOGD(TAG, "onStartCommand:: processor: " + processorName + ", reqParams: " + reqParams);

        // Instantiate the processor.
        Processor processorInstance;
        try {
            Class<?> cl = Class.forName(processorName);
            Constructor<?> cons = cl.getDeclaredConstructor(Bundle.class);
            processorInstance = (AbstractProcessor<?>) cons.newInstance(reqParams);

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Processor " + processorName + " does not exist");
        } catch (NoSuchMethodException |
                InvocationTargetException |
                InstantiationException |
                IllegalAccessException e) {
            if (LogUtils.DEBUG) e.printStackTrace();

            throw new IllegalStateException("Could not instantiate Processor " + processorName);
        }

        // Run processor
        addStartedObservable(startId, addRequest(processorInstance))
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        // Call to remove this Observable from
                        // list of pending started Observables.
                        startedObservableDone(startId);
                    }
                })
                .subscribe(new AndroidSubscriber() {
                    @Override
                    public void onNext(Object o) {
                        LOGD(TAG, "finished started observable:: processorName: " + processorName + ", reqParams" + reqParams);
                    }
                });

        return START_NOT_STICKY;
    }

    /**
     * Returns Handler tied to Applications MainLooper.
     * <p>
     * Processors can use this handler to run code on the application's
     * main thread.
     */
    public Handler getMainHandler() {
        return mRoboHandler;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // if onStartCommand has been called, this is considered to be a
        // started service. We must explicitly stop the service.
        stopServiceIfDone();
        return false;
    }

    /**
     * Adds the request to the work queue to be executed.
     * By default run on {@link rx.schedulers.Schedulers#io()} scheduler.
     * @param processor
     */
    public <T> Observable<T> addRequest(final Processor<T> processor) {
        //TODO: have to make sure the service is active whenever an observable is subscribed to
        // and therefore the observable code starts executing.
        return Observable.create( new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {

                LOGD(TAG, "Starting RoboService Observable stream");
                int resultCode = processor.run(RoboService.this);
                LOGD(TAG, "Finished with Processor: " + processor.getClass().getSimpleName() +
                " with Processor resultCode: " + resultCode + ", calling subscriber? " + !subscriber.isUnsubscribed());
                if (!subscriber.isUnsubscribed()) {
                    if (Processor.RS_SUCCESS == resultCode) {
                        subscriber.onNext(processor.getResult());
                        subscriber.onCompleted();
                    } else {
                        //TODO: handle request timeout errors.
                        subscriber.onError(
                                AbstractProcessor.createExceptionFromResultCode(resultCode, processor.toString()));
                    }
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Adds observable to list of started Observables.
     * <p>
     * This is useful for when started Observables want to stop the Service.
     *
     * @see #stopServiceIfDone()
     */
    private <T> Observable<T> addStartedObservable(int startId, Observable<T> observable) {
        mStartedObservables.put(startId, observable);
        return observable;
    }

    /**
     * Started Observables should call this method when done.
     * @param startId StartId passed in from {@link #onStartCommand(Intent, int, int)}.
     */
    private void startedObservableDone(int startId) {
        Observable observable = mStartedObservables.remove(startId);
        if (observable == null) {
            throw new IllegalStateException("Observable with startId " + startId
            + " not found in the mapping mStartedObservables");
        }
        stopServiceIfDone();
    }

    /**
     * Stops service if there are no more pending started Observables.
     */
    private void stopServiceIfDone() {
        if (mStartedObservables.size() == 0) {
            LOGD(TAG, "stopServiceIfDone:: all started observables done");
            // If there are still bounded clients, this does not stop this Service.
            stopSelf();
        }
    }

    /**
     * Class used for the client binder.
     */
    public class RoboticBinder extends Binder {

        public RoboService getService() {
            return RoboService.this;
        }

    }

}
