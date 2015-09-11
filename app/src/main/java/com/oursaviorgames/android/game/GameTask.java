package com.oursaviorgames.android.game;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Task for getting a game file.
 * First checks saved games,
 * NOTE: if multiple GameTasks run on multiple threads, they may step on each other toes,
 * if requests are to the same game.
 */
public class GameTask extends AsyncTask<String, Void, File>{

    private final static String TAG = makeLogTag(GameTask.class);

    /**
     * Status code if {@link GameTask} has finished with an error.
     */
    public final static int STATUS_ERROR = -1;
    /**
     * Status code if {@code GameTask} has finished successfully.
     */
    public final static int STATUS_SUCCESS = 1;

    /**
     * Flag for if game is expected to be saved.
     */
    public final static int FLAG_FROM_SAVED = 1;
    /**
     * Flag for if game is at most expected to be in cache.
     */
    public final static int FLAG_FROM_CACHE = 2;

    private final WeakReference<GameTaskListener> mListener;
    private final File mCacheDir;
    private final File mSavedDir;
    private final File mTempDir;
    private final int mFlag;

    /**
     * One of {@link GameTaskListener} status codes.
     */
    private int mStatus;

    /**
     * Constructor for GameTask.
     * Constructor is package private.
     * @param cacheDir
     * @param savedDir
     * @param flag one of {@code FLAG_FROM_CACHE} or {@code FLAG_FROM_SAVED}.
     * @param listener
     */
    GameTask(File cacheDir, File savedDir, File tempDir, int flag, GameTaskListener listener) {
        mCacheDir = cacheDir;
        mSavedDir = savedDir;
        mTempDir = tempDir;
        mFlag = flag;
        mListener = new WeakReference<>(listener);
    }

    /**
     *
     * @param params params[0] == gameId. params[1] == download url
     * @return File pointing to games directory or null if an error had occurred.
     */
    //TODO: needs complete refactoring.
    @Override
    protected File doInBackground(String... params) {
        // TODO: close all the streams in the finally block.
        final String gameKey = Util.getKeyFromGameId(params[0]);
        // Game directory to return.
        File returnGameDir;

        // Returns directory if game is in saved games.
        if ((returnGameDir = Util.findGameInDir(mSavedDir, gameKey)) != null) {
            mStatus = STATUS_SUCCESS;
            return returnGameDir;
        }
        // Returns directory if game is in cached games.
        // If flag is FLAG_FROM_SAVED, and game is in cache,
        // moves the game to saved games directory.
        if ((returnGameDir = Util.findGameInDir(mCacheDir, gameKey)) != null) {
            if (mFlag == FLAG_FROM_CACHE) {
                mStatus = STATUS_SUCCESS;
                return returnGameDir;
            } else if (mFlag == FLAG_FROM_SAVED) {
                returnGameDir = Util.moveDir(returnGameDir, mSavedDir);
                if (returnGameDir != null) {
                    mStatus = STATUS_SUCCESS;
                    return returnGameDir;
                } else {
                    mStatus = STATUS_ERROR;
                    return null;
                }
            }
        }

        // Game is not saved or cached, download from server.
        MediaHttpDownloader downloader =
                new MediaHttpDownloader(AndroidHttp.newCompatibleTransport(), null);
        File tempZipFile = new File(mTempDir, gameKey + ".zip");
        OutputStream out = null;
        try {
            out = new FileOutputStream(tempZipFile);
            GenericUrl requestUrl = new GenericUrl(params[1]);
            downloader.download(requestUrl, out);
        } catch (FileNotFoundException e) {
            LOGE(TAG, "Could not open zip file");
            mStatus = STATUS_ERROR;
        } catch (IOException e) {
            LOGE(TAG, "IOException downloading game file");
            //Download or closing the stream failed.
            mStatus = STATUS_ERROR;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing the OutputStream", e);
                }
            }
        }

        if (mStatus == STATUS_ERROR) {
            return null;
        }

        // File is downloaded into tempZip file.
        // Sets directory to unzip it to.
        // Either cache or saved.
        if (mFlag == FLAG_FROM_SAVED) {
            returnGameDir = new File(mSavedDir, gameKey);
        } else {
            returnGameDir = new File(mCacheDir, gameKey);
        }

        if (!Util.unpackZIP(tempZipFile, returnGameDir)) {
             //TODO: clean up all the stub files.
            mStatus = STATUS_ERROR;
            return null;
        }

        mStatus = STATUS_SUCCESS;
        return returnGameDir;
    }

    @Override
    protected void onPostExecute(File aFile) {
        if (mListener.get() != null) {
            mListener.get().onGameTaskFinished(aFile, mStatus);
        }
    }

    /**
     * Listener for events from {@link GameTask}.
     */
    public interface GameTaskListener {

        /**
         * Called when GameTask has finished execution.
         * A status code is returned depending if task finished
         * successfully, had errors or got cancelled.
         * @param gameDir Directory where the requested game resides.
         * @param status One of the STATUS_ codes in {@link GameTask}.
         */
        void onGameTaskFinished(File gameDir, int status);

    }
}
