package com.oursaviorgames.backend.auth;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;

import java.io.IOException;

import com.oursaviorgames.backend.Constants;
import com.oursaviorgames.backend.auth.response.GPlusTokenDebugResponse;
import com.oursaviorgames.backend.auth.response.TokenDebugResponse;
import com.oursaviorgames.backend.auth.response.FacebookTokenDebugResponse;
import com.oursaviorgames.backend.http.SimpleHttpRequestFactory;
import com.oursaviorgames.backend.utils.LogUtils;
import com.oursaviorgames.backend.utils.TimeUtils;

import static com.oursaviorgames.backend.utils.LogUtils.DEBUG_LOGI;
import static com.oursaviorgames.backend.utils.LogUtils.LOGI;

public class TokenDebugger{

    private static final String TAG = LogUtils.makeLogTag(TokenDebugger.class);

    /**
     * Number of seconds before a token expires that it is still considered valid.
     */
    public static final long TOKEN_VALIDITY_THRESHOLD = 5l * 60l;

    /**
     * Validates access {@code token} against {@code identityProvider}.
     * Callers should check for null in case the authentication fails.
     * This method performs no retries in case of failure.
     * @param identityProvider The {@link IdentityProvider} to check the {@code token} against.
     * @param token The access token to check. Must be the token itself.
     * @return A {@link ValidatedToken} or null if authentication fails or if arguments are null.
     */
    public ValidatedToken authenticate(IdentityProvider identityProvider, String token) {
        // Do not throw an error if null argument is passed.
        if (identityProvider != null && token != null) {
            return debugToken(identityProvider, token);
        }
        return null;
    }

    /**
     * <pre>
     * For a token to be valid:
     *      - Is debugged against the identity provider as being valid.
     *      - Token should be issued for this app.
     *      - Token does not expire within {@code EXPIRATION_VALIDITY_THRESHOLD} seconds
     *          of making this call.
     * </pre>
     *
     * @param identityProvider Cannot be null.
     * @param token Cannot be null.
     * @return ValidatedToken if the token is validated, null otherwise.
     */
    private static ValidatedToken debugToken(final IdentityProvider identityProvider, final String token) {
        // Performs debugging against the identity provider.
        TokenDebugResponse response = null;
        switch (identityProvider) {
            case GOOGLE_PLUS: {
                GPlusTokenDebugUrl url = GPlusTokenDebugUrl.buildUrl(token);
                response = performHttpRequest(url, GPlusTokenDebugResponse.class);
                break;
            }
            case FACEBOOK: {
                FacebookTokenDebugUrl url = FacebookTokenDebugUrl.buildUrl(token);
                response = performHttpRequest(url, FacebookTokenDebugResponse.class);
                break;
            }
        }
        // Checks validity of the token.
        if (response != null) {
            boolean isValid = response.isValid();
            isValid &= TimeUtils.compareToNow(response.getExpiryTime(), TOKEN_VALIDITY_THRESHOLD);
            if (identityProvider == IdentityProvider.FACEBOOK) {
                isValid &= response.getAppId().equals(Constants.FACEBOOK_APP_ID);
            }
            if (identityProvider == IdentityProvider.GOOGLE_PLUS) {
                if (response.getAppId().equals(Constants.GOOGLE_PLUS_APP_ID) ||
                        response.getAppId().equals(Constants.API_EXPLORER_CLIENT_ID)) {
                    isValid &= true;
                }
            }

            if (isValid) {
                // If token is validated, returns a ValidatedToken.
                return new ValidatedToken(token, identityProvider, response);
            }
        }
        // All else, return null.
        return null;
    }

    /**
     * Executes http request and parses JSON result as {@code dataClass}.
     * @param url The url of the request.
     * @param dataClass Parse data class. Should be a subclass of {@link TokenDebugResponse}.
     * @return Parsed JSON result as {@link TokenDebugResponse} or null if operation fails.
     */
    private static TokenDebugResponse performHttpRequest(GenericUrl url, Class dataClass){
        try {
            HttpRequest httpRequest = SimpleHttpRequestFactory.buildGetRequestWithJsonParser(url);
            HttpResponse httpResponse = httpRequest.execute();

            if (httpResponse.getStatusCode() == HttpStatusCodes.STATUS_CODE_OK) {
                return (TokenDebugResponse) httpResponse.parseAs(dataClass);
            } else {
                DEBUG_LOGI(TAG, "Failed debugging token. Got (" + httpResponse.getStatusMessage() +
                ") for request at (" + url.toString() + ").");
                return null;
            }
        } catch (IOException e) {
            LOGI(TAG, e.getMessage());
            return null;
        }
    }

}
