package com.oursaviorgames.android.catwalk;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;


import com.squareup.okhttp.OkHttpClient;

import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A secure resource client for a game.
 * Note: This resource client only responds to local resources so far.
 */
public class SecureResourceClient extends XWalkResourceClient {

    public final static String TAG = makeLogTag(SecureResourceClient.class);

    /* Uri schemes */
    private final static String SCHEME_FILE = "file";
    private final static String SCHEME_HTTP = "http";
    private final static String SCHEME_HTTPS = "https";

    private final Context mContext;
    private final OkHttpClient client;
    private final Uri mGameDir;

    /**
     * Default Constructor.
     * @param view
     * @param gameDir Directory where game is located.
     */
    public SecureResourceClient(Context context, XWalkView view, File gameDir) throws IOException {
        super(view);
        mContext = context;
        client = CatHttpClient.getHttpClient(context);
        mGameDir = Uri.fromFile(gameDir);
    }

    @Override
    public void onLoadStarted(XWalkView view, String url) {
        LOGD(TAG, "onLoadStarted:: url: " + url);
        super.onLoadStarted(view, url);
    }

    @Override
    public void onLoadFinished(XWalkView view, String url) {
        LOGD(TAG, "onLoadFinished:: url: " + url);
        super.onLoadFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
        LOGD(TAG, "shouldOverrideUrlLoading:: url:" + url);
        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * Intercepts load requests. If resource is locally available,
     * provides the {@link android.webkit.WebResourceResponse}.
     *
     * @param view
     * @param url
     * @return
     */
    @Override
    public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
        // Validate url as safe if its a file request.
        Uri reqUri = Uri.parse(url);
        if (reqUri.getScheme().equals(SCHEME_FILE)) {
            File file = new File(reqUri.getPath());
            if (file.exists()) {
                if (isLocalUriSafe(mGameDir, reqUri)) {
                    try {
                        LOGD(TAG, "loading url from cache. url: " + url);
                        String mimeType = URLConnection.guessContentTypeFromName(url);
                        InputStream data = new FileInputStream(reqUri.getPath());
                        return new WebResourceResponse(mimeType, null, data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // Use default implementation
        LOGD(TAG, "not intercepting. url " + url);
        return super.shouldInterceptLoadRequest(view, url);
    }

    /**
     * Checks if {@code rootUri} is parent of {@code uri},
     * and that {@code uri} doesn't point outside it's parent.
     * (rejects uri paths containing '..'!)
     * @param rootUri
     * @param uri
     * @return
     */
    private static boolean isLocalUriSafe(Uri rootUri, Uri uri) {

        boolean startsAtRoot = uri.toString().startsWith(rootUri.toString());
        boolean backtracks = uri.toString().contains("..");
        return startsAtRoot && !backtracks;
    }
}
