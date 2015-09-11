package com.oursaviorgames.android.ui.menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.PrefUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * A menu item base class.
 * Subclasses can provide their own widget to be put inside the menu item,
 * by overriding {@link #getWidget()}. However subclasses should not hold
 * a string reference to the created widget.
 * <p>
 * Initialization callback order:
 * <p>
 * {@link #onSetInitialValue()} -> {@link #onCreateView(android.view.ViewGroup)}.
 * <p>
 * If a MenuItem overrides {@link #getIntent()}, Activity described by the Intent
 * is launched whenever the MenuItem is clicked, otherwise {@link #onClick(android.view.View)}
 * callback is called passing the previously created widget from {@link #getWidget()} if any.
 * <p>
 *
 */
public class MenuItem {

    private static final String TAG = makeLogTag(MenuItem.class);

    private Context mContext;
    private String mTitle;
    private Intent mIntent;

    public MenuItem(Context context, int titleResId) {
        mContext = context;
        mTitle = context.getString(titleResId);
    }

    public MenuItem(Context context, int titleResId, Intent intent) {
        this(context, titleResId);
        mIntent = intent;
    }

    protected Context getContext() {
        return mContext;
    }

    protected String getTitle() {
        return mTitle;
    }

    protected Intent getIntent() {
        return mIntent;
    }

    /**
     * Subclasses should override this function to pass in their preferences key,
     * if their storing any value by calling of persist* functions.
     */
    public String getPreferenceKey() {
        return null;
    }

    /**
     * Called before {@link #onCreateView(android.view.ViewGroup)} to load
     * any previously persisted state if any.
     * Previously persisted state can be loaded by calling one of getPersisted* functions.
     */
    public void onSetInitialValue() {

    }

    /**
     * Subclasses should override this function if they want to add a widget
     * to the MenuItem.
     */
    protected View getWidget() {
        return null;
    }

    /**
     * Called when the menu item is clicked on.
     * @param widget Menu item widget or null if no widget has been passed.
     */
    protected void onClick(View widget) {
        LOGD(TAG, "onClick is called");
    }

    /**
     * Called after {@link #onSetInitialValue()}
     * @param parent
     * @return
     */
    public View onCreateView(ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_menu_layout, parent, false);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(mTitle);
        FrameLayout widgetFrame = (FrameLayout) view.findViewById(R.id.widget_frame);
        View widget = getWidget();
        if (widget != null) {
            widget.setClickable(false); // Widgets should only display data.
            widgetFrame.addView(widget);
        } else {
            widgetFrame.setVisibility(View.GONE);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout widgetFrame = (FrameLayout) v.findViewById(R.id.widget_frame);
                Intent intent = getIntent();
                if (intent != null) {
                    mContext.startActivity(intent);
                } else {
                    if (widgetFrame.getChildCount() > 1 ) {
                        throw new IllegalStateException("Widget frame should only have one child");
                    } else {
                        MenuItem.this.onClick(widgetFrame.getChildAt(0));
                    }
                }
            }
        });
        return view;
    }

    protected boolean getPersistedBoolean(boolean defValue) {
        checkKey();
        SharedPreferences sp = PrefUtils.getSharedPrefs(mContext);
        return sp.getBoolean(getPreferenceKey(), defValue);
    }

    protected String getPresistedString(String defValue) {
        checkKey();
        SharedPreferences sp = PrefUtils.getSharedPrefs(mContext);
        return sp.getString(getPreferenceKey(), defValue);
    }

    protected void persistBoolean(boolean value) {
        checkKey();
        SharedPreferences.Editor editor = PrefUtils.getEditor(mContext);
        editor.putBoolean(getPreferenceKey(), value);
        editor.apply();
    }

    protected void persistString(String value) {
        checkKey();
        SharedPreferences.Editor editor = PrefUtils.getEditor(mContext);
        editor.putString(getPreferenceKey(), value);
        editor.apply();
    }

    /** Validates key returned by getPreferenceKey */
    private void checkKey() {
        if (getPreferenceKey() == null) {
            throw new IllegalStateException("Preference key is not set");
        }
    }

}
