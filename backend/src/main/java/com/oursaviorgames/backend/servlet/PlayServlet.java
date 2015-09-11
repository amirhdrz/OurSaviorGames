package com.oursaviorgames.backend.servlet;

import com.oursaviorgames.backend.model.datastore.Game;

import net.sf.uadetector.OperatingSystemFamily;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.service.UADetectorServiceFactory;

import org.apache.http.HttpStatus;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.Utils.decodeBase64String;

public class PlayServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

        final String userAgentStr = req.getHeader("User-Agent");
        final String gameIdBase64 = req.getParameter("g");
        final long gameId;

        // Checks request.
        if (userAgentStr == null) {
            resp.sendError(HttpStatus.SC_BAD_REQUEST, "User-Agent header not set.");
            return;
        }
        try {
            gameId = decodeBase64String(gameIdBase64);
        } catch (NullPointerException e) {
            //TODO: show better error message to the user. maybe redirect to home page.
            setErrorRedirect(resp);
            return;
        } catch (IllegalArgumentException e) {
            //TODO: show a better error message to the user. Maybe a 404, instead of 400.
            resp.sendError(HttpStatus.SC_BAD_REQUEST, "Illegal Game Id parameter");
            return;
        }

        //TODO: add cache to user agent parser.
        UserAgentStringParser uaParser = UADetectorServiceFactory.getResourceModuleParser();
        ReadableUserAgent userAgent = uaParser.parse(userAgentStr);
        final boolean isUserAgentAndroid =
                userAgent.getOperatingSystem().getFamily().equals(OperatingSystemFamily.ANDROID);

        // Find requested Game.
        Game requestedGame = findGameEntity(gameId);
        if (requestedGame == null) {
            //TODO: show user a better 404.
            resp.sendError(HttpStatus.SC_NOT_FOUND, "Game with id " + gameId + " not found");
            return;
        }

        if (isUserAgentAndroid) {
            req.setAttribute("gameIdBase64", gameIdBase64);
            req.getRequestDispatcher("/play.jsp").forward(req, resp);
        } else {
            final String originUrl = requestedGame.getOriginUrl();
            if (originUrl != null) {
                resp.setStatus(HttpStatus.SC_MOVED_PERMANENTLY);
                resp.sendRedirect(requestedGame.getOriginUrl());
            } else {
                //TODO: tell the user that the game is only playable on our app.
                resp.sendError(HttpStatus.SC_NOT_FOUND, "Redirect link not found");
            }
        }
    }

    /**
     * Retrieves game entity with gameId.
     * If game is not found or is not published, returns null.
     */
    private Game findGameEntity(long gameId) {
        //TODO: show a more specific error to the user if game is
        //TODO: implement this in GameSerivce
        // simply not published, or gameId is actually not found.
        Game game = ofy().load().key(Game.createKeyFromId(gameId)).now();
        return (game != null && game.isPublished())? game : null;
    }

}
