package com.oursaviorgames.android.ui;

import android.widget.AbsListView;

import java.util.LinkedList;

/**
 * This object becomes the listener for a {@link AbsListView} on-scroll events,
 * and dispatches those events to all interested listeners.
 */
public class OnScrollEventDispatcher implements AbsListView.OnScrollListener {

    private LinkedList<AbsListView.OnScrollListener> mListeners = new LinkedList<>();

    /**
     * This object sets itself to be listView's on scroll listener.
     * All other object interested in listening to this listView's scroll events,
     * should add themselves by calling {@link #addListener(AbsListView.OnScrollListener)}.
     */
    public OnScrollEventDispatcher(AbsListView listView) {
        if (listView == null) {
            throw new IllegalArgumentException("listView cannot be null");
        }
        listView.setOnScrollListener(this);
    }

    /**
     * Adds listener to be notified of scroll events.
     */
    public void addListener(AbsListView.OnScrollListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Removes listener from being notified of scrolling events.
     * @param listener The listener to remove.
     */
    public void removeListener(AbsListView.OnScrollListener listener) {
        mListeners.remove(listener);
    }

    /**
     * Clears all {@link AbsListView.OnScrollListener}s.
     */
    public void clearListeners() {
        mListeners.clear();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        for (AbsListView.OnScrollListener listener : mListeners) {
            listener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        for (AbsListView.OnScrollListener listener : mListeners) {
            listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }
}
