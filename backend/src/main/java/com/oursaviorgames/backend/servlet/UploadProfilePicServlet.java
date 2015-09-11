package com.oursaviorgames.backend.servlet;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;
import com.google.common.io.ByteStreams;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.oursaviorgames.backend.auth.AuthorizationHeaders;
import com.oursaviorgames.backend.auth.User;
import com.oursaviorgames.backend.gcs.CloudStorage;
import com.oursaviorgames.backend.gcs.GcsFile;
import com.oursaviorgames.backend.model.datastore.UserProfile;
import com.oursaviorgames.backend.service.UserService;
import com.oursaviorgames.backend.utils.AuthUtils;
import com.oursaviorgames.backend.utils.TextUtils;

import static com.oursaviorgames.backend.service.OfyService.ofy;
import static com.oursaviorgames.backend.utils.LogUtils.LOGE;
import static com.oursaviorgames.backend.utils.LogUtils.makeLogTag;
import static com.oursaviorgames.backend.utils.TextUtils.emptyStringIfNull;

/**
 * Handles users uploading pictures.
 * <p>
 * Re-sizes and stores profile picture and a thumbnail in WEBP format,
 */
public class UploadProfilePicServlet extends HttpServlet {

    private static final String TAG = makeLogTag(UploadProfilePicServlet.class);

    private static final long MAX_CONTENT_SIZE = 10 * 1024 * 1024; // 10MB, max content size to read.

    /**
     * Maximum image size in pixels.
     */
    private static final int MAX_IMAGE_DIMENS_PX = 1024;

    /**
     * Thumbnail image size in pixels.
     */
    private static final int THUMBNAIL_DIMENS_PX = 144;

    /**
     * This is where backoff parameters are configured. Here it is aggressively retrying with
     * backoff, up to 10 times but taking no more that 15 seconds total to do so.
     */
    private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
            .initialRetryDelayMillis(10)
            .retryMaxAttempts(10)
            .totalRetryPeriodMillis(15000)
            .build());

    @Override
    public void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws ServletException, IOException {
        // Authenticates user.
        final UserProfile userProfile;
        try {
            User user = AuthUtils.throwIfNotAuthenticated(httpRequest);
            UserService userService = UserService.UserServiceFactory.createInstance();
            userProfile = userService.getUserProfile(UserProfile.createKey(user));
        } catch (UnauthorizedException e) {
            LOGE(TAG, "Unauthorized user. UserId:" + emptyStringIfNull(httpRequest.getHeader(AuthorizationHeaders.USER_ID)));
            httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
            httpResponse.getWriter().println(e.getMessage());
            return;
        }

        // Reads image data from request.
        final byte[] imageData;
        try {
            InputStream safeInputStream = ByteStreams.limit(httpRequest.getInputStream(), MAX_CONTENT_SIZE);
            imageData = ByteStreams.toByteArray(safeInputStream);
        } catch (IOException e) {
            LOGE(TAG, emptyStringIfNull(e.getMessage()));
            httpResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Transforms the image and stores it in Google Cloud Storage.
        final Image newImage;
        final Image thumbnailImage;
        try {
            final ImagesService imagesService = ImagesServiceFactory.getImagesService();

            // We need to make a copy of the image data for creating the thumbnail image,
            // since ImagesService corrupts the original Image object.
            final Image image = ImagesServiceFactory.makeImage(imageData);
            final Image imageCopy = ImagesServiceFactory.makeImage(imageData);

            int maxDimen = Math.max(image.getHeight(), image.getWidth());
            Transform resize;
            if (maxDimen > MAX_IMAGE_DIMENS_PX) {
                // Resize without stretching the image.
                resize = ImagesServiceFactory.makeResize(MAX_IMAGE_DIMENS_PX, MAX_IMAGE_DIMENS_PX, false);
            } else {
                resize = ImagesServiceFactory.makeResize(image.getWidth(), image.getHeight());
            }

            newImage = imagesService.applyTransform(resize, image, ImagesService.OutputEncoding.WEBP);

            Transform thumbnailResize = ImagesServiceFactory.makeResize(THUMBNAIL_DIMENS_PX, THUMBNAIL_DIMENS_PX, false);
            thumbnailImage = imagesService.applyTransform(thumbnailResize, imageCopy, ImagesService.OutputEncoding.WEBP);

        } catch (IllegalArgumentException e) {
            // Thrown if imageData is null or invalid.
            LOGE(TAG, "Invalid image data");
            httpResponse.setStatus(HttpStatus.SC_BAD_REQUEST);
            return;
        } catch (ImagesServiceFailureException e) {
            LOGE(TAG, "ImagesService failed");
            httpResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Generate new GcsFilename
        GcsFilename newProfileImageFile = CloudStorage.generateProfileImageFile(userProfile.getId());
        GcsFilename newThumbImageFile = CloudStorage.getThumbnailProfilePicFile(newProfileImageFile);

        // Builds GcsOptions with public-read and cache control.
        final GcsFileOptions fileOptions = new GcsFileOptions.Builder()
                .acl("public-read")
                .cacheControl("max-age=31556926")
                .mimeType("image/webp")
                .build();

        // Copy the content of the request as an image to GCS.
        try {
            // Writing profile picture.
            GcsOutputChannel outputChannel = gcsService.createOrReplace(newProfileImageFile, fileOptions);
            outputChannel.write(ByteBuffer.wrap(newImage.getImageData()));

            // Writing thumbnail.
            GcsOutputChannel thumbnailOutputChannel = gcsService.createOrReplace(newThumbImageFile, fileOptions);
            thumbnailOutputChannel.write(ByteBuffer.wrap(thumbnailImage.getImageData()));

            // Close both streams.
            outputChannel.close();
            thumbnailOutputChannel.close();

            // Delete old profile image and thumbnail if any.
            try {
                if (userProfile.getProfileImageFile() != null) {
                    final GcsFilename oldProfileImage = userProfile.getProfileImageFile().createGcsFileName();
                    final GcsFilename oldThumbnailImage = CloudStorage.getThumbnailProfilePicFile(oldProfileImage);
                    gcsService.delete(oldProfileImage);
                    if (oldThumbnailImage != null) gcsService.delete(oldThumbnailImage);
                }
            } catch (NullPointerException e) {
                //TODO: we shouldn't need to catch NullPointerExceptions.
                LOGE(TAG, e.toString());
            }
            // Updates to image files finished successfully.
            // Update user's profile image.
            userProfile.setProfileImageFile(GcsFile.createGcsFile(newProfileImageFile));
            ofy().save().entity(userProfile).now();
            // Success.
            httpResponse.setStatus(HttpStatus.SC_CREATED);
        } catch (IOException e) {
            LOGE(TAG, "Error while copying data");
            httpResponse.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

    }

}
