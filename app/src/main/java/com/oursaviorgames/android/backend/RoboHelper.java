package com.oursaviorgames.android.backend;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameCollectionResponse;
import com.appspot.robotic_algebra_633.mobileApiEndpoint.model.GameResponse;

import java.util.ArrayList;

import com.oursaviorgames.android.auth.CredentialService;
import com.oursaviorgames.android.backend.processor.EndpointProcessor;
import com.oursaviorgames.android.backend.processor.FeedbackProcessor;
import com.oursaviorgames.android.backend.processor.FileDownloadProcessor;
import com.oursaviorgames.android.backend.processor.GameProcessors;
import com.oursaviorgames.android.backend.processor.GenerateUsernameEndpoint;
import com.oursaviorgames.android.backend.processor.GetUserProfileEndpoint;
import com.oursaviorgames.android.backend.processor.CommentProcessors;
import com.oursaviorgames.android.backend.processor.PlayTokenProcessors;
import com.oursaviorgames.android.backend.processor.UpdateUserProfileProcessor;
import com.oursaviorgames.android.backend.processor.UploadProfilePicProcessor;
import com.oursaviorgames.android.backend.processor.UserSignupProcessor;
import com.oursaviorgames.android.data.GameContract;
import com.oursaviorgames.android.data.UserAccount;
import rx.Observable;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;
import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * Contains convenience functions to make http requests using {@link RoboService}.
 */
public class RoboHelper {

    private static final String TAG = makeLogTag(RoboHelper.class);

    private final RoboService       robo;
    private final CredentialService cred;

    public RoboHelper(RoboService robo, CredentialService cred) {
        this.robo = robo;
        this.cred = cred;
    }

    /*
        ------------------------
        Endpoints
        ------------------------
     */

    public Observable<Boolean> getUserProfile() {
        Bundle p = createAuthParams(robo, cred);
        return robo.addRequest(new GetUserProfileEndpoint(p));
    }

    public Observable<Void> updateUserProfile(String newUsername) {
        Bundle p = createAuthParams(robo, cred);
        p.putString(UpdateUserProfileProcessor.PARAM_NEW_USERNAME, newUsername);
        return robo.addRequest(new UpdateUserProfileProcessor(p));
    }

    public Observable<Void> signupUser(String name, String username, String gender) {
        //TODO: add gcm id
        Bundle p = createAuthParams(robo, cred);
        p.putString(UserSignupProcessor.PARAM_NAME, name);
        p.putString(UserSignupProcessor.PARAM_USER_NAME, username);
        p.putString(UserSignupProcessor.PARAM_GENDER, gender);
        p.putString(UserSignupProcessor.PARAM_DEVICE_ID, "gcm id");
        return robo.addRequest(new UserSignupProcessor(p));
    }

    public void uploadProfilePicture() {
        Bundle p = createAuthParams(robo, cred);
        RoboService.startProcessor(robo.getApplicationContext(), UploadProfilePicProcessor.class, p);
    }

    public Observable<String> generateUsername(String name) {
        Bundle p = createAuthParams(robo, cred);
        p.putString(GenerateUsernameEndpoint.PARAM_NAME, name);
        return robo.addRequest(new GenerateUsernameEndpoint(p));
    }

    public Observable<GameResponse> getRandomGame() {
        Bundle p = createAuthParams(robo, cred);
        return robo.addRequest(new GameProcessors.RandomGame(p));
    }

    public Observable<Void> updateSpotlightGame(boolean forceUpdate) {
        Bundle p = createAuthParams(robo, cred);
        p.putBoolean(GameProcessors.GetSpotlight.PARAM_FORCE_UPDATE, forceUpdate);
        return robo.addRequest(new GameProcessors.GetSpotlight(p));
    }

    public Observable<Void> updateGameList(Uri gameList, boolean loadMore) {
        Bundle p = createAuthParams(robo, cred);
        // Whether we're refreshing the list or loading more items.
        p.putBoolean(GameProcessors.GameList.PARAM_LOAD_MORE, loadMore);
        // Which game list we're updating.
        if (GameContract.HotGameEntry.CONTENT_URI.equals(gameList)) {
            p.putString(GameProcessors.GameList.PARAM_SORT_ORDER, GameProcessors.GameList.SORT_VALUE_POPULAR);
        } else if (GameContract.NewGameEntry.CONTENT_URI.equals(gameList)) {
            p.putString(GameProcessors.GameList.PARAM_SORT_ORDER, GameProcessors.GameList.SORT_VALUE_NEW);
        } else {
            throw new IllegalArgumentException("Game list (" + gameList + ") is not valid");
        }
        return robo.addRequest(new GameProcessors.GameList(p));
    }

    public Observable<GameCollectionResponse> getGames(ArrayList<String> gameIds) {
        Bundle p = new Bundle(1);
        p.putStringArrayList(GameProcessors.Get.PARAM_GAME_IDS, gameIds);
        return robo.addRequest(new GameProcessors.Get(p));
    }

    public Observable<String> getComments(String gameId, @Nullable String nextPageToken) {
        checkNotNull(gameId);
        Bundle p = createAuthParams(robo, cred);
        p.putString(CommentProcessors.Get.PARAM_GAME_ID, gameId);
        p.putString(CommentProcessors.Get.PARAM_NEXT_PAGE_TOKEN, nextPageToken);
        return robo.addRequest(new CommentProcessors.Get(p));
    }

    public void postComment(String gameId, String comment) {
        checkNotNull(gameId, "Null gameId");
        checkNotNull(comment, "Null comment");
        Bundle p = createAuthParams(robo, cred);
        p.putString(CommentProcessors.Post.PARAM_GAME_ID, gameId);
        p.putString(CommentProcessors.Post.PARAM_COMMENT, comment);
        RoboService.startProcessor(robo.getApplicationContext(), CommentProcessors.Post.class, p);
    }

    public void deleteComment(long commentId) {
        Bundle p = createAuthParams(robo, cred);
        p.putLong(CommentProcessors.Delete.PARAM_COMMENT_ID, commentId);
        RoboService.startProcessor(robo.getApplicationContext(), CommentProcessors.Delete.class, p);
    }

    public void flagComment(long commentId) {
        Bundle p = createAuthParams(robo, cred);
        p.putLong(CommentProcessors.Flag.PARAM_COMMENT_ID, commentId);
        RoboService.startProcessor(robo.getApplicationContext(), CommentProcessors.Flag.class, p);
    }

    public Observable<Void> sendFeedback(String email, String msg) {
        Bundle p = createAuthParams(robo, cred);
        p.putString(FeedbackProcessor.PARAM_EMAIL, email);
        p.putString(FeedbackProcessor.PARAM_MESSAGE, msg);
        return robo.addRequest(new FeedbackProcessor(p));
    }

    public Observable<Uri> downloadFile(String url, Uri dest) {
        Bundle p = new Bundle();
        p.putString(FileDownloadProcessor.PARAM_DOWNLOAD_URL, url);
        p.putString(FileDownloadProcessor.PARAM_DEST_URI, dest.toString());
        return robo.addRequest(new FileDownloadProcessor(p));
    }

    public void uploadPlayTokens() {
        Bundle p = createAuthParams(robo, cred);
        RoboService.startProcessor(robo.getApplicationContext(), PlayTokenProcessors.Send.class, p);
    }

    /*
        --------
        Helpers
        --------
     */

    /**
     * Utility method for creating Bundle that contains
     * authentication parameters.
     * Uses {@link UserAccount} to set the 'X-UserId' header.
     * @return Bundle.
     */
    public static Bundle createAuthParams(Context context, CredentialService cred) {
        //TODO: Should cache all of this?
        if (cred.getAccessToken() == null) {
            throw new IllegalStateException("CredentialService should have a valid token, got null");
        }
        Bundle bundle = new Bundle();
        bundle.putString(EndpointProcessor.PARAM_ACCESS_TOKEN, cred.getAccessToken());
        bundle.putString(EndpointProcessor.PARAM_IDENTITY_PROVIDER_DOMAIN, cred.getName().getDomain());
        final String userId = UserAccount.getUserAccount(context).getUserId();
        if ( userId != null) {
            bundle.putString(EndpointProcessor.PARAM_USER_ID, userId);
        }
//        LOGD(TAG, "Created AuthParams: " + bundle.toString());
        return bundle;
    }

}
