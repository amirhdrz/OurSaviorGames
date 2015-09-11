package com.oursaviorgames.android.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpointRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} for syncing local user data with the backend server.
 */
@Deprecated
public class SyncService extends IntentService {

    // Actions SyncService can perform.
    private static final String ACTION_SYNC_ALL_ADAPTERS = "co.catalogg.android.service.action.sync_all";

    // Registered SyncAdapters.
    private static final Class[] registeredSyncAdapters = {
    };

    // Instantiated list of registered SyncAdapters.
    private List<SyncAdapter> syncAdapters;

    /**
     * Starts the {@link SyncService} syncing all {@link SyncAdapter}s.
     * @param context
     */
    public static void startSyncAll(Context context) {
        Intent intent = new Intent(context, SyncService.class);
        intent.setAction(ACTION_SYNC_ALL_ADAPTERS);
        context.startService(intent);
    }

    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            // Instantiate Sync adapters.
            syncAdapters = instantiateSyncAdapters();

            final String action = intent.getAction();
            if (ACTION_SYNC_ALL_ADAPTERS.equals(action)) {
                handleSyncAllAdapters();
            }
        }
    }

    /**
     * Handles syncing all the adapters.
     */
    private void handleSyncAllAdapters() {
        // Loops through all SyncAdapters and executes their request if sync is needed.
        for (SyncAdapter adapter : syncAdapters) {
            if (adapter.isSyncNeeded()) {
                MobileApiEndpointRequest request = adapter.getRequest();
                if (request != null) {
                    try {
                        request.execute();
                        adapter.onResponse(request.getLastStatusCode());
                    } catch (IOException e) {
                        // Wait for the next call to SyncService to try syncing again.
                    }
                }
            }
        }
    }

    /**
     * Instantiates registered SyncAdapters and sets their {@link Context}.
     * @return
     */
    private List<SyncAdapter> instantiateSyncAdapters() {
        List<SyncAdapter> syncAdapters = new ArrayList<SyncAdapter>(registeredSyncAdapters.length);
        for (int i = 0 ; i < registeredSyncAdapters.length; i++) {
            try {
                SyncAdapter adapter = (SyncAdapter) registeredSyncAdapters[i].newInstance();
                adapter.setContext(getApplicationContext());
                syncAdapters.add(adapter);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return syncAdapters;
    }

}
