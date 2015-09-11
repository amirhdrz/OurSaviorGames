package com.oursaviorgames.android.ui.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.oursaviorgames.android.R;

import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class MenuAdapter extends BaseAdapter {

    private static final String TAG = makeLogTag(MenuAdapter.class);

    private static final int MENU_ITEM_TYPE    = 0;
    private static final int DIVIDER_ITEM_TYPE = 1;

    private Context        mContext;
    private MenuCategory[] mMenu;

    public MenuAdapter(Context context, MenuCategory[] menu) {
        mContext = context;
        mMenu = menu;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % 2 == 0) {
            return MENU_ITEM_TYPE;
        } else {
            return DIVIDER_ITEM_TYPE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return (mMenu.length * 2) - 1;
    }

    @Override
    public Object getItem(int position) {
        return mMenu[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (MENU_ITEM_TYPE == getItemViewType(position)) {
            MenuCategory category = mMenu[position / 2];
            category.onSetInitialValue();
            view =  category.onCreateView(parent);
        } else if (DIVIDER_ITEM_TYPE == getItemViewType(position)) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_menu_divider, parent, false);
        }
        return view;
    }
}
