package com.oursaviorgames.android.ui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.oursaviorgames.android.R;

public class MenuCategory extends MenuItem {

    private List<MenuItem> mItems;

    public MenuCategory(Context context, int titleResId) {
        super(context, titleResId);
        mItems = new ArrayList<>(2);
    }

    public void addMenuItem(MenuItem item) {
        mItems.add(item);
    }

    @Override
    public void onSetInitialValue() {
        for (MenuItem item : mItems) {
            item.onSetInitialValue();
        }
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        LinearLayout rootView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_menu_category_layout, parent, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        title.setText(getTitle());
        for (MenuItem item : mItems) {
            View itemView = item.onCreateView(rootView);
            rootView.addView(itemView);
        }
        return rootView;
    }
}
