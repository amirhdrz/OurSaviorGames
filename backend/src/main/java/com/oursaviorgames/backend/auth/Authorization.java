package com.oursaviorgames.backend.auth;

import javax.servlet.http.HttpServletRequest;

import com.oursaviorgames.backend.http.HeaderInvalidException;
import com.oursaviorgames.backend.http.HeaderNotPresentException;
import com.oursaviorgames.backend.http.HeaderTooLongException;

import static com.oursaviorgames.backend.utils.Preconditions.checkNotNull;

/**
 * An immutable wrapper class for the authorization headers for an HTTP request
 * that's making authenticated calls.
 * Access token and identity provider are always present, however user id may be null.
 */
public final class Authorization {

    private final String accessToken;

    private final IdentityProvider identityProvider;

    private final Long userId;

    public Authorization(String accessToken, IdentityProvider identityProvider, Long userId) {
        this.accessToken = checkNotNull(accessToken, "AccessToken cannot be null");
        this.identityProvider = checkNotNull(identityProvider, "IdentityProvider cannot be null");
        this.userId = userId;
    }

    /**
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return The identity provider.
     */
    public IdentityProvider getIdentityProvider() {
        return identityProvider;
    }

    /**
     * @return The user id if it was present in the http request, or null.
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Parses the HTTP request for authorization headers as defined in {@link AuthorizationHeaders}.
     * @param request The HTTP request from the client.
     * @return An {@link Authorization} object.
     * @throws HeaderNotPresentException If one of the headers is not present, expect for 'X-UserId' header.
     * @throws HeaderTooLongException If access token header is longer than 500 characters.
     * @throws HeaderInvalidException If the 'X-UserId' header doesn't parse as a long number if it is
     *                                  present in the header.
     */
    public static Authorization parseHttpRequest(HttpServletRequest request)
    throws HeaderTooLongException, HeaderInvalidException, HeaderNotPresentException {
        // Reads headers from the request.
        String tokenHeader = request.getHeader(AuthorizationHeaders.AUTHORIZATION);
        String identityProviderHeader = request.getHeader(AuthorizationHeaders.IDENTITY_PROVIDER);
        String userIdHeader = request.getHeader(AuthorizationHeaders.USER_ID);
        try {
            if (tokenHeader.length() > 500) {
                throw new HeaderTooLongException("Authorization header too long");
            }
            IdentityProvider identityProvider = IdentityProvider.parseDomain(identityProviderHeader);
            String accessToken = getTokenFromAuthorizationHeader(tokenHeader);
            // If user-id is present, try to parse it.
            // otherwise return an Authorization object without user-id field present.
            if (userIdHeader != null) {
                Long userId = Long.valueOf(userIdHeader);
                return new Authorization(accessToken, identityProvider, userId);
            } else {
                return new Authorization(accessToken, identityProvider, null);
            }
        } catch (NullPointerException e) {
            throw new HeaderNotPresentException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new HeaderInvalidException("Passed in userId in http request is not a number");
        }
    }

    /**
     * Extracts access token from 'Authorization' header.
     * @param authorization Field value of 'Authorization' header.
     * @return
     */
    //TODO: is this even compliant with wc3 specification.
    private static String getTokenFromAuthorizationHeader(String authorization) {
        if (authorization.length() > 7 && authorization.substring(0, 6).equals("Bearer"))
            return authorization.substring(7);
        else
            return authorization;
    }

}
