package com.oursaviorgames.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.ui.widget.PigeonHoleImageView;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.FileUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGW;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class CropperActivity extends BaseActivity {

    private static final String TAG = makeLogTag(CropperActivity.class);

    private static final int IMAGE_PICKER_RQST_CODE = 1;

    @InjectView(R.id.pigeonView)
    PigeonHoleImageView mPigeonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);
        ButterKnife.inject(this);

        // Opens image picker
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent. CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICKER_RQST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICKER_RQST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                mPigeonView.setImageURI(data.getData());
            } else {
                // User didn't select an image,
                // return Activity result.
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    /**
     * Cancel button clicked.
     * @param view
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.cancel)
    public void onCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Done button clicked.
     * @param view
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.done)
    public void onDone(View view) {
        File croppedImage = new File(FileUtils.getDir(this, FileUtils.DIR.TEMP), "cropped-profile.webp");
        Intent result = new Intent();
        try {
            mPigeonView.saveCroppedImageToFile(croppedImage);
            result.setData(Uri.fromFile(croppedImage));
            setResult(RESULT_OK, result);
        } catch (IOException e) {
            // Show error to user and close Activity
            LOGW(TAG, "saveCroppedImageToFile failed:" + e.getMessage());
            ErrorUtils.showError(this, R.string.error_crop);
            setResult(RESULT_CANCELED);
        } finally {
            finish();
        }
    }

}
