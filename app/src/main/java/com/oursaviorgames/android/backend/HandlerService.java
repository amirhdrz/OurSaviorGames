package com.oursaviorgames.android.backend;

import android.app.Service;
import android.os.Handler;

/**
 * A {@link Service} that can also return a {@link Handler}
 * tied to applications main thread.
 */
public abstract class HandlerService extends Service {

    /**
     * Returns Handler tied to application's MainLooper.
     */
    public abstract Handler getMainHandler();

    
}
