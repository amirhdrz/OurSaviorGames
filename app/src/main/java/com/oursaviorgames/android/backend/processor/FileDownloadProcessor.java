package com.oursaviorgames.android.backend.processor;

import android.net.Uri;
import android.os.Bundle;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.OutputStream;

import com.oursaviorgames.android.backend.HandlerService;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Downloads files and puts them in the desired directory.
 *
 * TODO: add caching to FileProcessor
 */
public class FileDownloadProcessor extends OkHttpProcessor<Uri> {

    private static final String TAG = makeLogTag(FileDownloadProcessor.class);

    /**
     * Download Url
     * <P>Type: String</P>
     */
    public static final String PARAM_DOWNLOAD_URL = "downloadUrl";

    /**
     * Path name of the file where the
     * <P>Type: Uri String</P>
     */
    public static final String PARAM_DEST_URI = "destUri";

    private Uri destUri;

    public FileDownloadProcessor(Bundle reqParams) {
        super(reqParams);
        destUri = Uri.parse(reqParams.getString(PARAM_DEST_URI));
    }

    @Override
    protected int processRequest(HandlerService context, Bundle reqParams, OkHttpClient client) {
        // Builds the request.
        Request request = new Request.Builder()
                .url(reqParams.getString(PARAM_DOWNLOAD_URL))
                .build();
        // Executes the request.
        try {
            LOGD(TAG, "Downloading Url: " + reqParams.getString(PARAM_DOWNLOAD_URL));
            Response response = client.newCall(request).execute();
            // Output file.
            OutputStream out = context.getContentResolver().openOutputStream(destUri);
            // Copy the response byte-stream to output stream.
            // It then closes both streams.
            copy(response.body().byteStream(), out);
            if (response.isSuccessful()) {
                LOGD(TAG, "Success downloading Url: " + reqParams.getString(PARAM_DOWNLOAD_URL));
                return RS_SUCCESS;
            } else {
                LOGD(TAG, "Failed: status code: " + response.code());
                return convertHttpStatusCode(response.code());
            }
        } catch (IOException e) {
            return RS_IO_EXCEPTION;
        }
    }

    @Override
    protected void onSuccess(HandlerService context) {
        //TODO: is on success useful here?
    }

    @Override
    protected void onFailure(HandlerService context, int resultCode) {

    }
    @Override
    protected Uri onGetResult() {
        return destUri;
    }

    @Override
    public String toString() {
        return FileDownloadProcessor.class.getSimpleName() + " " + super.toString();
    }
}
