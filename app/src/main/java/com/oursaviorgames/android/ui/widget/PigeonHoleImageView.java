package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.external.photoview.PhotoViewAttacher;

import static com.oursaviorgames.android.util.LogUtils.LOGW;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class PigeonHoleImageView extends ImageView {

    private static final String TAG = makeLogTag(PigeonHoleImageView.class);

    // WEBP image compression quality.
    private static final int WEBP_COMPRESSION_QUALITY = 100;
    // Cropped image is scaled to this size.
    private static final int CROPPED_IMAGE_SIZE       = 540;
    private static final int BG_COLOR_RES_ID          = R.color.cropperBackground;

    private static Paint sBgPaint;
    private static Paint sHolePaint;

    private PhotoViewAttacher mAttacher;
    private Matrix mMatrix = new Matrix();
    private Bitmap pigeonHoleBitmap;
    private float  holeRadius;
    private int    w, h;
    private float halfW;

    public PigeonHoleImageView(Context context) {
        super(context);
        init();
    }

    public PigeonHoleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PigeonHoleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (sBgPaint == null) {
            sBgPaint = new Paint();
            sBgPaint.setAntiAlias(true);
            sBgPaint.setColor(getContext().getResources().getColor(BG_COLOR_RES_ID));
            sBgPaint.setStyle(Paint.Style.FILL);
        }
        if (sHolePaint == null) {
            sHolePaint = new Paint();
            sHolePaint.setAntiAlias(true);
            sHolePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        //TODO: we're prematurely leaking reference to this object.
        // make sure PhotoViewAttacher doesn't do anything fancy.
        mAttacher = new PhotoViewAttacher(this);
        mAttacher.setScaleType(ScaleType.CENTER);
    }

    /**
     * Decodes {@code uri} in a background thread.
     * NOTE: {@link #getDrawable()} will return null until background thread completes.
     * @param uri image uri.
     */
    @Override
    public void setImageURI(Uri uri) {
        new BitmapWorkerTask(getContext(), this, uri).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w / 2, h / 2, oldw, oldh);
        this.w = w;
        this.h = h;
        halfW = w / 2.f;
        holeRadius = halfW;
        pigeonHoleBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(pigeonHoleBitmap);
        canvas.drawPaint(sBgPaint);
        canvas.drawCircle(halfW, h / 2.f, holeRadius, sHolePaint);
    }

    public float getHoleOffsetTop() {
        return (h - 2.f * holeRadius) / 2.f;
    }

    public float getHoleDiameter() {
        return 2.f * holeRadius;
    }

    public PhotoViewAttacher getPhotoViewAttacher() {
        return mAttacher;
    }

    @Override
    public void setImageMatrix(Matrix matrix) {
        mMatrix.set(matrix);
        super.setImageMatrix(matrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pigeonHoleBitmap != null) {
            canvas.drawBitmap(pigeonHoleBitmap, 0, 0, null);
        }
    }

    /**
     * Returns Rect with coordinates relative to the view's drawable.
     * Respects the pigeon hole.
     * @return
     */
    private RectF getCroppedRect() {
        final float d = getHoleDiameter();
        final float topOffset = getHoleOffsetTop();
        final float s = mAttacher.getScale();

        // displayRect: Rect surrounding whole image relative to display.
        RectF displayRect = mAttacher.getDisplayRect();

        // holeRect: Pigeon hole Rect relative to display.
        RectF holeRect = new RectF(0.f, topOffset, d, topOffset + d);

        // Translate to get hole rect relative to drawable from hole rect relative to display.
        // Scale against PhotoViewAttacher scale value.
        Matrix m = new Matrix();
        m.setTranslate(-displayRect.left, -displayRect.top);
        m.postScale(1.f / s, 1.f / s);
        m.mapRect(holeRect);
        return holeRect;
    }

    /**
     * Returns cropped {@link Bitmap} of this view's drawable.
     * @return {@link Bitmap} containing the cropped image.
     */
    private Bitmap getCroppedBitmap() {
        // Converts Drawable to Bitmap
        Drawable d = getDrawable();
        Bitmap src = ((BitmapDrawable) d).getBitmap();

        // Gets the cropped rect and calculate floor of all values.
        RectF rectF = getCroppedRect();
        Rect rect = new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);

        // Crop image.
        return Bitmap.createBitmap(src, rect.left, rect.top, rect.width(), rect.height());
    }

    /**
     * This is a blocking function, until image is cropped and saved to file.
     * @param destFile The file to store the cropped image.
     * @throws java.io.IOException Thrown if file is not found or there was error closing the stream.
     */
    public void saveCroppedImageToFile(File destFile)
            throws IOException {
        OutputStream os = new FileOutputStream(destFile);
        Bitmap bitmap = getCroppedBitmap();
        boolean success = bitmap.compress(Bitmap.CompressFormat.WEBP, WEBP_COMPRESSION_QUALITY, os);
        if (!success) {
            throw new IOException("Bitmap compression failed");
        }
        os.close();
    }

    /**
     * Loads a Bitmap into memory by checking its dimensions first,
     * if the image is too large, it is subsampled.
     */
    private static class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {

        private static final int MAX_IMAGE_SIZE = 1024;

        private WeakReference<PigeonHoleImageView> imageViewRef;
        private WeakReference<Context>             contextRef;
        private Uri                                mUri;

        public BitmapWorkerTask(Context context, PigeonHoleImageView imageView, Uri uri) {
            this.imageViewRef = new WeakReference<>(imageView);
            this.contextRef = new WeakReference<>(context);
            mUri = uri;
        }

        /**
         * Decodes the image specified by uri.
         * @return full sized or down-sampled bitmap.
         *          If Uri could not be opened, returns null.
         */
        @Override
        protected Bitmap doInBackground(Void... params) {
            Context context = contextRef.get();
            if (context != null) {

                // For the sake of simplicity we're just using a simple
                // InputSteam and opening the steam twice, rather than
                // buffering it.
                InputStream stream = null;
                try {
                    // Reading the image size.
                    stream = context.getContentResolver().openInputStream(mUri);
                    BitmapFactory.Options options = readBitmapDimens(stream);
                    stream.close();

                    // Decoding Bitmap
                    stream = context.getContentResolver().openInputStream(mUri);
                    options.inJustDecodeBounds = false; // We want to read the bitmap data.
                    options.inSampleSize = calculateInSampleSize(options, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
                    return BitmapFactory.decodeStream(stream, null, options);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGW(TAG, "Could not open mUri: " + mUri.toString());
                    return null;
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            LOGW(TAG, "Unable to close steam from Uri: " + mUri.toString());
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            final PigeonHoleImageView imageView = imageViewRef.get();
            final Context context = contextRef.get();
            if (imageView != null && context != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    // Calculates the scaling value.
                    final float holeDiameter = imageView.getHoleDiameter();
                    final float bitmapSmallestWidth = Math.min(bitmap.getWidth(), bitmap.getHeight());
                    final float r = bitmapSmallestWidth / holeDiameter;
                    final float minScale;
                    // If the image's smallest width is larger than the hole diameter,
                    // then downscale, otherwise upscale.
                    if (holeDiameter < bitmapSmallestWidth) {
                        minScale = Math.min(r, 1.f / r);
                    } else {
                        minScale = Math.max(r, 1.f / r);
                    }
                    // Modify the PhotoViewAttacher scale properties
                    PhotoViewAttacher attacher = imageView.getPhotoViewAttacher();
                    attacher.setScaleBounds(minScale, minScale * 3f);
                    attacher.setScale(minScale, true);
                } else {

                    Toast.makeText(context, "Image could not be loaded", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int width = options.outWidth;
            final int height = options.outHeight;

            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2
                // until both dimensions are smaller than request size.
                while ((halfHeight / inSampleSize) > reqHeight
                        || (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        private static BitmapFactory.Options readBitmapDimens(InputStream in) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // We're just interested in the image meta-data.
            BitmapFactory.decodeStream(in, null, options);
            return options;
        }

    }

}
