package com.oursaviorgames.backend.utils;

import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.utils.SystemProperty;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.oursaviorgames.backend.auth.Authorization;
import com.oursaviorgames.backend.auth.TokenDebugger;
import com.oursaviorgames.backend.auth.User;
import com.oursaviorgames.backend.auth.UserAuthenticator;
import com.oursaviorgames.backend.auth.ValidatedToken;
import com.oursaviorgames.backend.http.HttpHeaderException;
import com.oursaviorgames.backend.model.datastore.Admin;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGI;

/**
 * Utilities for user authentications.
 */
public class AuthUtils {

    private static final String TAG = LogUtils.makeLogTag(AuthUtils.class);

    /**
     * Authenticates user, on failure throws exception.
     * @param httpRequest Http request from client.
     * @return {@link com.oursaviorgames.backend.auth.User}.
     * @throws UnauthorizedException Thrown if authentication fails.
     */
    public static User throwIfNotAuthenticated(HttpServletRequest httpRequest)
            throws UnauthorizedException {
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            throw new UnauthorizedException("User is not authorized.");
        }
        return user;
    }

    /**
     * Utility method for authenticating users.
     * Try using {@link AuthUtils#throwIfNotAuthenticated(HttpServletRequest)} instead.
     * @param request Client HTTP request.
     * @return {@link User} object or null if authentication fails.
     */
    private static User getAuthenticatedUser(HttpServletRequest request) {
        try {
            Authorization authorization = Authorization.parseHttpRequest(request);
            UserAuthenticator authenticator = new UserAuthenticator();
            return authenticator.authenticate(authorization);
        } catch (HttpHeaderException e) {
            LOGI(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Debugs access token from Http request.
     * @param request Client HTTP request.
     * @return {@link ValidatedToken} or null if token could not be debugged.
     */
    public static ValidatedToken debugAccessToken(HttpServletRequest request) {
        try {
            Authorization authorization = Authorization.parseHttpRequest(request);
            TokenDebugger tokenDebugger = new TokenDebugger();
            return tokenDebugger.authenticate(authorization.getIdentityProvider(), authorization.getAccessToken());
        } catch (HttpHeaderException e) {
            LOGI(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Returns Ip address of the client request.
     * @param httpRequest Client http request.
     * @return Ip address.
     */
    public static String getIpAddress(HttpServletRequest httpRequest) {
        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }
        return ipAddress;
    }

    public static void throwIfNotAdmin(com.google.appengine.api.users.User user) throws UnauthorizedException {
        if (!isUserAdmin(user)) {
            throw new UnauthorizedException("Unauthorized request");
        }
    }

    public static void throwIfNotDevMode() throws ForbiddenException {
        if (SystemProperty.environment.value() ==
                SystemProperty.Environment.Value.Production) {
            throw new ForbiddenException("Only to be used while testing");
        }
    }

    /**
     * Use this method to check if user is admin.
     * @param user
     * @return
     */
    public static boolean isUserAdmin(com.google.appengine.api.users.User user){
        if (user == null) {
            return false;
        }
        List<Admin> admins = ofy().load().type(Admin.class).list();
        for (Admin admin : admins) {
            if (user.getUserId().equals(admin.getUserId())) {
                return true;
            }
        }
        return false;
    }

}
