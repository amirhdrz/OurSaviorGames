package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;

import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.oursaviorgames.android.backend.HandlerService;

/**
 * A processor with a OkHttpClient.
 */
public abstract class OkHttpProcessor<T> extends AbstractProcessor<T> {

    /**Used below to determine the size of chucks to read in. Should be > 1kb and < 10MB */
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private static OkHttpClient sOkHttpClient = new OkHttpClient();

    public OkHttpProcessor(Bundle requestParams) {
        super(requestParams);
    }

    @Override
    protected final int processRequest(HandlerService context, Bundle requestParams) {
        return processRequest(context, requestParams, sOkHttpClient);
    }

    /**
     * @param context
     * @param reqParams
     * @param client OkHttpClient to use for making http calls.
     * @return One of RS_ result codes.
     */
    abstract protected int processRequest(HandlerService context, Bundle reqParams, OkHttpClient client);

    /**
     * Transfer the data from the inputStream to the outputStream. Then close both streams.
     */
    protected static void copy(InputStream input, OutputStream output) throws IOException {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = input.read(buffer);
            while (bytesRead != -1) {
                output.write(buffer, 0, bytesRead);
                bytesRead = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }

}
