package com.oursaviorgames.android.backend.processor;

import android.os.Bundle;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.*;

import com.oursaviorgames.android.backend.BackendUrls;
import com.oursaviorgames.android.backend.HandlerService;
import com.oursaviorgames.android.data.UserDataContract;
import com.oursaviorgames.android.data.metastore.MetaStore;
import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.LOGE;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Uploads user's profile picture.
 * Checks {@link UserDataContract.Extra#META_PROFILE_PICTURE_UPLOADED} flag.
 * Only if set to false, uploads profile picture.
 */
public class UploadProfilePicProcessor extends AuthedOkHttpProcessor<Void> {

    private static final String TAG = makeLogTag(UploadProfilePicProcessor.class);

    private static MediaType IMAGE_MEDIA_TYPE = MediaType.parse("image/*");

    public UploadProfilePicProcessor(Bundle requestParams) {
        super(requestParams);
    }

    @Override
    protected int processRequest(HandlerService context, Bundle reqParams, OkHttpClient client, Request.Builder authedBuilder) {
        final boolean previouslyUploaded = MetaStore.getMetaStore(context).getMeta(UserDataContract.Extra.META_PROFILE_PICTURE_UPLOADED);

        LOGD(TAG, "processing UploadProfilePic: previouslyUploaded: " + previouslyUploaded);

        if (!previouslyUploaded) {

            File profileImage = FileUtils.getProfilePictureFile(context);
            if (profileImage == null || !profileImage.exists()) {
                LOGE(TAG, "Null profile picture");
                return RS_FAILED;
            }

            Request request = authedBuilder.url(BackendUrls.getProfileUploadUrl())
                    .post(RequestBody.create(IMAGE_MEDIA_TYPE, profileImage))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    LOGD(TAG, "Success uploading profile picture");
                    return RS_SUCCESS;
                } else {
                    LOGE(TAG, "Failed: status code: " + response.code());
                    return convertHttpStatusCode(response.code());
                }
            } catch (java.io.IOException e) {
                e.printStackTrace();
                return RS_IO_EXCEPTION;
            }
        } else {
            return RS_CANCELLED;
        }
    }

    @Override
    protected void onSuccess(HandlerService context) {
        // Store the success result to meta_store.
        MetaStore.getMetaStore(context)
                .putMeta(UserDataContract.Extra.META_PROFILE_PICTURE_UPLOADED, true);
    }

    @Override
    protected void onFailure(HandlerService context, int resultCode) {
        // Do nothing.
    }

    @Override
    protected Void onGetResult() {
        return null;
    }
}

