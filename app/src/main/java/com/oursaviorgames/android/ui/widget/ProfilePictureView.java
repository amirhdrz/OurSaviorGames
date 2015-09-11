package com.oursaviorgames.android.ui.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.io.File;
import java.io.IOException;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.UserDataContract;
import com.oursaviorgames.android.data.metastore.MetaStore;
import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * Loads and saves user's previous profile picture.
 * <p>
 * Can set a new profile picture by calling {@link #setProfileImageUri(android.net.Uri)}.
 * <p>
 * To save current profile picture, call {@link #saveProfilePicture()}.
 */
public class ProfilePictureView extends View {

    private static final String TAG = makeLogTag(ProfilePictureView.class);

    private static final int   BG_COLOR_RES_ID     = R.color.primary;
    private static final int   BORDER_COLOR_RES_ID = R.color.darkHighlight;
    private static final float BORDER_STROKE_DIP   = 8.f;

    private static int sBgColor = -1;
    private static float sBorderSize;
    private static Paint sBorderPaint;
    private static Paint sHolePaint;

    /**
     * {@code mCurrentUri} should always
     */
    private Uri mCurrentUri;
    private boolean mProfileChanged = false;
    private Bitmap mUnsetProfileBitmap; // Bitmap used to store currently set profile pic if the view has not been laid out.
    private Bitmap mProfileBitmap;
    private Bitmap mFgBitmap; // The foreground bitmap, includes the background color and the hole.
    private int    w;

    public ProfilePictureView(Context context) {
        super(context);
        init();
    }

    public ProfilePictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfilePictureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        sBorderSize = convertDipToPx(BORDER_STROKE_DIP);
        if (sBgColor == -1) {
            sBgColor = getContext().getResources().getColor(BG_COLOR_RES_ID);
        }
        if (sBorderPaint == null) {
            sBorderPaint = new Paint();
            sBorderPaint.setAntiAlias(true);
            sBorderPaint.setColor(getContext().getResources().getColor(BORDER_COLOR_RES_ID));
            sBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
        if (sHolePaint == null) {
            sHolePaint = new Paint();
            sHolePaint.setAntiAlias(true);
            sHolePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        // Loads previously saved profile picture
        Uri savedImage = Uri.fromFile(FileUtils.getProfilePictureFile(getContext()));
        loadImage(savedImage);
    }

    /**
     * Only opens Uri with scheme {@link android.content.ContentResolver#SCHEME_FILE}.
     * @param uri
     */
    public void setProfileImageUri(Uri uri) {
        loadImage(uri);
        mProfileChanged = true;
    }

    /**
     * Loads image file located at uri, and sets it as the current bitmap and uri.
     * @param uri
     */
    private void loadImage(Uri uri) {
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            File imageFile = new File(uri.getPath());
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                if (bitmap != null) {
                    // If there was a previously saved profile picture.
                    setProfileBitmap(bitmap);
                    mCurrentUri = uri;
                }
            }
        } else {
            throw new IllegalArgumentException("Uri scheme not supported. Original Uri: " + uri.toString());
        }
    }


    private void setProfileBitmap(Bitmap bm) {
        if (bm != null) {
            //TODO: don't just scale the image, try center cropping with scaling.
            int bmWidth = bm.getWidth();
            int bmHeight = bm.getHeight();
            int smallestWidth = Math.min(bmWidth, bmHeight);
            Bitmap croppedBitmap = Bitmap.createBitmap(bm, bmWidth - smallestWidth, 0, smallestWidth, smallestWidth);

            if (w != 0) {
                mProfileBitmap = Bitmap.createScaledBitmap(croppedBitmap, w, w, true);
                invalidate();
            } else {
                mUnsetProfileBitmap = croppedBitmap;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        final float halfW = w/2.f;

        // Create foreground bitmap, which includes the background color
        // the circular stroke and the hole in the middle.
        mFgBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mFgBitmap);
        canvas.drawColor(getContext().getResources().getColor(BG_COLOR_RES_ID));
        canvas.drawColor(sBgColor);
        canvas.drawCircle(halfW, halfW, halfW, sBorderPaint);
        canvas.drawCircle(halfW, halfW, halfW - sBorderSize, sHolePaint);

        if (mUnsetProfileBitmap != null) {
            mProfileBitmap = Bitmap.createScaledBitmap(mUnsetProfileBitmap, w, w, true);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProfileBitmap != null) {
            canvas.drawBitmap(mProfileBitmap, 0.f, 0.f, null);
        }
        canvas.drawBitmap(mFgBitmap, 0.f, 0.f, null);
    }

    /**
     * Saves currently selected profile picture, or returns
     * immediately if nothing has been changed.
     * This function blocks.
     * This function simply copy's last call the last call
     * (if any) to {@link #setProfileImageUri(android.net.Uri)}
     * to the profile picture file.
     * @return true if new profile picture is saved, false if profile pictuer hasn't chagned.
     * @throws java.io.IOException
     */
    public boolean saveProfilePicture() throws IOException {
        if (mProfileChanged) {
            LOGD(TAG, "saveProfilePicture:: saving profile picture");
            // mUri has to still be valid by this point.
            File src = new File(mCurrentUri.getPath());
            org.apache.commons.io.FileUtils.copyFile(src, FileUtils.getProfilePictureFile(getContext()));
            // Reset the profile picture uploaded flag.
            MetaStore.getMetaStore(getContext()).putMeta(UserDataContract.Extra.META_PROFILE_PICTURE_UPLOADED, false);
            return true;
        }
        return false;
    }

    private float convertDipToPx(float dips) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

}
