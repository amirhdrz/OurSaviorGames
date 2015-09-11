package com.oursaviorgames.backend.servlet;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.cmd.Query;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oursaviorgames.backend.model.datastore.Game;
import com.oursaviorgames.backend.model.datastore.PlaySnapshot;
import com.oursaviorgames.backend.model.datastore.PlayToken;
import com.oursaviorgames.backend.service.ExtraGameService;
import com.oursaviorgames.backend.service.GameService;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGW;
import static com.oursaviorgames.backend.utils.LogUtils.makeLogTag;


/**
 * Processes {@link com.oursaviorgames.backend.model.datastore.PlayToken}s
 */
public class ProcessPlayTokensCron extends HttpServlet {

    private static final String TAG = makeLogTag(ProcessPlayTokensCron.class);

    /**
     * Chunk size to be used by the query.
     */
    //TODO: optimize the chunk size.
    private static final int CHUNK_SIZE = 1000;

    /**
     * Creates {@link QueryResultIterator} to iterate over a {@link PlayToken}s
     * which are not counted.
     * <p>
     * Result set is sorted by {@link PlayToken#F_GameId} field.
     *
     * @param startCursor Start cursor from previous query or null.
     */
    private static QueryResultIterator<PlayToken> createQueryResultIterator(String startCursor) {
        // Builds play-token fetching query with large internal chunk size.
        Query<PlayToken> query = ofy()
                .load()
                .type(PlayToken.class)
                .filter(PlayToken.F_Counted, false)
                .order(PlayToken.F_GameId)
                .chunk(CHUNK_SIZE)
                .limit(10 * CHUNK_SIZE);

        // Sets start cursor if any from request.
        if (startCursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(startCursor));
        }

        return query.iterator();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        // Get parameters from request.
        final String startCursor = req.getParameter("cursor");

        final QueryResultIterator<PlayToken> queryIterator = createQueryResultIterator(startCursor);

        // Linked list to hold the snapshotList.
        final LinkedList<PlaySnapshot> snapshotList = new LinkedList<>();

        // Flag for the possibility of further more play-tokens in the datastore.
        boolean continu = false;

        // Iterates over the play-tokens and creates snapshotList.
        while(queryIterator.hasNext()) {
            PlayToken playToken = queryIterator.next();
            // Creates snapshot for a new game-id.
            if (snapshotList.isEmpty() || playToken.getGameId() != snapshotList.getLast().getGameId()) {
                PlaySnapshot snapshot = new PlaySnapshot(playToken.getGameId());
                snapshotList.add(snapshot);
            }
            // Updates the last snapshot in the linked list.
            snapshotList.getLast().updateCounter(playToken.getPlayDuration());
            // Sets playToken as counted.
            playToken.setCounted();
            ofy().save().entity(playToken);

            continu = true;
        }

        // Processing snapshotList if any was created.
        // Saves the snapshotList into the datastore.
        ofy().save().entities(snapshotList).now();

        // For each snapshot, update game's play count.
        for (PlaySnapshot snapshot : snapshotList) {
            Game game = ofy().load().key(Game.createKeyFromId(snapshot.getGameId())).now();
            if (game == null) {
                LOGW(TAG, "Null game entity with id(" + snapshot.getGameId() + ")");
            } else {
                game.updateStats(snapshot.getPlayCount(), snapshot.getPlayDuration());
                game.setHotScore(hotscore(game.getDatePublished(), game.getPlayCount()));
                ofy().save().entity(game);
            }
        }

        // Sets the OK response before queueing the next job.
        resp.setStatus(HttpServletResponse.SC_OK);

        // If there are no more tokens to be processed, update game caches,
        // else add a new job to process remaining tokens.
        if (!continu) {
            recacheAllGames();
        } else {
            Cursor cursor = queryIterator.getCursor();
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(TaskOptions.Builder
                    .withUrl("/cron/processPlayTokens")
                    .param("cursor", cursor.toWebSafeString())
                    .method(TaskOptions.Method.GET));
        }
    }

    // Re-caches all games caches
    private void recacheAllGames() {
        GameService.GameServiceFactory.createInstance().reCache();
        ExtraGameService.ExtraGameServiceFactory.getInstance().forceRecache();
    }

    private static long hotscore(Date publishedDate, long playCount) {
        long age = (publishedDate.getTime() / 1000l) - 1415804779; // age in seconds.
        long ageFactor = age / 45000l;
        double o = Math.log10(Math.max(playCount, 1l)) * 500.0;
        return (long) o + ageFactor;
    }

}

