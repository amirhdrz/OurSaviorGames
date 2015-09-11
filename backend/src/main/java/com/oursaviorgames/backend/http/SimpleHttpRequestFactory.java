package com.oursaviorgames.backend.http;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;

/**
 * Builds {@link HttpRequest}s.
 * For maximum-efficiency use this class to build all Http requests.
 * This class contains thread-safe shared resources for building Http requests.
 */
public final class SimpleHttpRequestFactory {

    /**
     * Thread-safe globally shared HttpTransport object.
     */
    static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Thread-safe globally shared JsonFactory object.
     */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /**
     * Builds a http get request with Json parser.
     * @param url The url of the Http request.
     * @return {@link com.google.api.client.http.HttpRequest}.
     * @throws IOException
     */
    public static HttpRequest buildGetRequestWithJsonParser(GenericUrl url) throws IOException {
        com.google.api.client.http.HttpRequestFactory requestFactory =
                HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) throws IOException {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });
        return requestFactory.buildGetRequest(url);
    }

}
