package com.oursaviorgames.android.catwalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;

import org.apache.commons.io.IOUtils;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.io.InputStream;

import com.oursaviorgames.android.R;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class CatWalkView extends XWalkView {

    private static final String TAG = makeLogTag(CatWalkView.class);

    public CatWalkView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CatWalkView(Context context, Activity activity) {
        super(context, activity);
        init();
    }

    private void init() {
        LOGD(TAG, "init");
        Context context = getContext();
        String JSinit = readJSResource(context, R.raw.catwalk_init);
        evaluateJavascript(JSinit, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                LOGD(TAG, "JS init done: " + value);
            }
        });
    }

    public Bitmap getBitmap() {
        TextureView textureView = findXWalkTextureView(this);
        return textureView.getBitmap();
    }

    //TODO: very hacky. find a better solution.
    private static TextureView findXWalkTextureView(ViewGroup group) {
        int childCount = group.getChildCount();
        for(int i=0;i<childCount;i++) {
            View child = group.getChildAt(i);
            if(child instanceof TextureView) {
                String parentClassName = child.getParent().getClass().toString();
                boolean isRightKindOfParent = (parentClassName.contains("XWalk"));
                if(isRightKindOfParent) {
                    return (TextureView) child;
                }
            } else if(child instanceof ViewGroup) {
                TextureView textureView = findXWalkTextureView((ViewGroup) child);
                if(textureView != null) {
                    return textureView;
                }
            }
        }

        return null;
    }

    /**
     * Reads .js file pointed to by resId.
     * @return script as String or null.
     */
    private static String readJSResource(Context context, int resId) {
        InputStream in = context.getResources().openRawResource(resId);
        try {
            String JSString = IOUtils.toString(in, "UTF-8");
            return JSString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}