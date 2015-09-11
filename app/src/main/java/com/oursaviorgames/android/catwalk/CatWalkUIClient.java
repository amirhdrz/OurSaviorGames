package com.oursaviorgames.android.catwalk;


import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
* Custom CatWalk UI client.
*/
public class CatWalkUIClient extends XWalkUIClient {

    private static final String TAG = makeLogTag(CatWalkUIClient.class);

    /**
     * Default constructor.
     * @param view
     */
    public CatWalkUIClient(XWalkView view) {
        super(view);
    }

    @Override
    public void onScaleChanged(XWalkView view, float oldScale, float newScale) {
        super.onScaleChanged(view, oldScale, newScale);
        LOGD(TAG, "onScaleChanged:: oldScale: " + oldScale + ", newScale: " + newScale);
    }

    @Override
    public void onPageLoadStarted(XWalkView view, String url) {
        super.onPageLoadStarted(view, url);
        LOGD(TAG, "onPageLoadStarted:: url: " + url);
    }

    @Override
    public void onPageLoadStopped(XWalkView view, String url, LoadStatus status) {
        super.onPageLoadStopped(view, url, status);
        LOGD(TAG, "onPageLoadStopped:: status: " + status.toString() + ", url: " + url);
    }
}

