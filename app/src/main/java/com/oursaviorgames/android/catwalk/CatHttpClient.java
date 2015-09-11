package com.oursaviorgames.android.catwalk;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;

import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class CatHttpClient {

    private static final String TAG = makeLogTag(CatHttpClient.class);

    private static final long CACHE_MAX_SIZE = 500 * 1024 * 1024; // 500MB

    private final OkHttpClient mOkHttpClient;

    private static CatHttpClient sInstance;

    public static OkHttpClient getHttpClient(Context context) throws IOException {
        if (sInstance == null) {
            sInstance = new CatHttpClient(context);
        }
        return sInstance.mOkHttpClient;
    }

    private CatHttpClient(Context context) throws IOException {
        Cache cache = new Cache(FileUtils.getDir(context, FileUtils.DIR.GAME_CACHE), CACHE_MAX_SIZE);
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setCache(cache);
    }



}
