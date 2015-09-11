package com.oursaviorgames.android.ui.menu;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.oursaviorgames.android.R;

public final class CheckBoxMenuItem extends MenuItem {

    private final boolean mDefaultValue;
    private final String mPreferenceKey;
    private boolean mChecked = false;

    public CheckBoxMenuItem(Context context, int titleResId, String preferenceKey, boolean defValue) {
        super(context, titleResId);
        mPreferenceKey = preferenceKey;
        mDefaultValue = defValue;
    }

    @Override
    public String getPreferenceKey() {
        return mPreferenceKey;
    }

    @Override
    public void onSetInitialValue() {
        mChecked = getPersistedBoolean(mDefaultValue);
    }

    @Override
    protected View getWidget() {
        CheckBox widget = new CheckBox(getContext());
        widget.setButtonDrawable(getContext().getResources().getDrawable(R.drawable.checkbox_button));
        widget.setChecked(mChecked);
        return widget;
    }

    @Override
    protected void onClick(View widget) {
        super.onClick(widget);
        mChecked = !mChecked;
        ((CheckBox) widget).setChecked(mChecked);
        persistBoolean(mChecked);
    }
}
