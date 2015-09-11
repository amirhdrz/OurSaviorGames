package com.oursaviorgames.android.game;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;

import com.oursaviorgames.android.backend.BackendUrls;
import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Manages game cache and saved games.
 * Returns Uri's to game directories on game requests.
 * Downloads requested game over the network if not cached or saved.
 * TODO: add a queue for queuing downloads and save queue to disk,
 * TODO: need a cache ejector for cached games.
 * TODO: multiple game tasks issued on same directory shouldn't step on each others toes.
 * implements a locking mechanism.
 * // and resume from it when appropriate.
 */
public class GameLibrarian {

    private static final String TAG = makeLogTag(GameLibrarian.class);

    private final File mCacheDir;
    private final File mSavedDir;
    private final File mTempDir;

    public GameLibrarian(Context context) throws IOException {
        mCacheDir = FileUtils.getCachedGamesDir(context);
        mSavedDir = FileUtils.getSavedGamesDir(context);
        mTempDir = FileUtils.getDir(context, FileUtils.DIR.TEMP);
    }

    /**
     * Returns a asynchronous task for getting game.
     * <B>GameTask is already executed. Handle is returned only to cancel a game request.</B>
     * @param gameId
     * @param listener
     * @return
     */
    public GameTask getGame(String gameId, GameTask.GameTaskListener listener) {
        //TODO: loading only from cache for now.
        GameTask task =
                new GameTask(mCacheDir, mSavedDir, mTempDir, GameTask.FLAG_FROM_CACHE, listener);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, gameId, BackendUrls.getGameDownloadUrl(gameId));
        return task;
    }
}
