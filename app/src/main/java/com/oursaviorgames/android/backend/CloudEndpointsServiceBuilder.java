package com.oursaviorgames.android.backend;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.MobileApiEndpoint;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

import com.oursaviorgames.android.Constants;
import com.oursaviorgames.android.util.Preconditions;

/**
 * Backend service provider.
 */
//TODO: set application name
public class CloudEndpointsServiceBuilder {

    private static final HttpTransport sHttpTransport = AndroidHttp.newCompatibleTransport();
    private static final JsonFactory   sJsonFactory   = new AndroidJsonFactory();

    @Deprecated
    public static MobileApiEndpoint getService() {
        return new MobileApiEndpoint.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null).setApplicationName(Constants.APPLICATION_NAME).build();
    }

    /** Builds an instance of MobileApiEndpoint with HTTP request initialized with the provided
     * fields.
     * Implementation is not thread-safe. Only build the service on the main thread.
     * @param accessToken User access token or null. Set to Authorization header if not null.
     * @param identityProvider Must be present along side accessToken.
     * @param userId Internal user id or null. Used for server optimization.
     * @return new instance of MobileApiEndpoint.
     */
    public static MobileApiEndpoint buildApiEndpoint(final String accessToken, final String identityProvider, final String userId) {
        HttpRequestInitializer requestInitializer = new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {
                if (accessToken != null) {
                    Preconditions.checkNotNull(identityProvider);
                    request.getHeaders().setAuthorization("Bearer " + accessToken);
                    request.getHeaders().set(AuthorizationHeaders.HEADER_IDENTITY_PROVIDER, identityProvider);
                }
                if (userId != null) {
                    request.getHeaders().set(AuthorizationHeaders.HEADER_USER_ID, userId);
                }
            }
        };
        return new MobileApiEndpoint.Builder(sHttpTransport, sJsonFactory, requestInitializer).build();
    }

}
