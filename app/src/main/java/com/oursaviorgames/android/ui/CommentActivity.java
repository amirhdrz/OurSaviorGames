package com.oursaviorgames.android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.backend.RoboHelper;
import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.data.UserAccount;
import com.oursaviorgames.android.rx.AndroidSubscriber;
import com.oursaviorgames.android.util.ErrorUtils;
import com.oursaviorgames.android.util.TypefaceUtils;
import com.oursaviorgames.android.util.UiUtils;
import com.oursaviorgames.android.util.Utils;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.oursaviorgames.android.data.SqlUtils.and;
import static com.oursaviorgames.android.data.SqlUtils.equal;
import static com.oursaviorgames.android.data.SqlUtils.neq;
import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

public class CommentActivity extends AuthedActivity {

    private static final String TAG = makeLogTag(CommentActivity.class);

    private static final String EXTRA_GAME_ID = "game_id";

    /**
     * Starts Comment Activity.
     * Checks for internet availability before starting Activity.
     * @param gameId Game to load.
     */
    public static void startActivity(Context context, String gameId) {
        // If user is offline, show offline error message.
        if (Utils.isNetworkAvailable(context)) {
            if (TextUtils.isEmpty(gameId)) {
                throw new IllegalArgumentException("gameId is null");
            }
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra(EXTRA_GAME_ID, gameId);
            context.startActivity(intent);
        } else {
            ErrorUtils.showOfflineError(context);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        final String gameId = getIntent().getStringExtra(EXTRA_GAME_ID);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, CommentFragment.newInstance(gameId))
                    .commit();
        }
    }

    /**
     * Comment fragment.
     */
    public static class CommentFragment extends AuthedRoboHelperFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String TAG = makeLogTag(CommentFragment.class);

        private static final String ARG_GAME_ID = "game_id";

        private static final int COMMENT_LOADER_ID = 1;

        @InjectView(R.id.listView)
        ListView mListView;
        @InjectView(R.id.emptyView)
        View mEmptyView;
        @InjectView(R.id.commentEditText)
        EditText mCommentEditBox;
        @InjectView(R.id.sendCommentButton)
        ImageView mSendButton;
        @InjectView(R.id.loadingView)
        View mLoadingView;

        CommentAdapter mAdapter;
        String mGameId;
        String mNextPageToken = null;

        public static CommentFragment newInstance(String gameId) {
            if (TextUtils.isEmpty(gameId)) {
                throw new IllegalArgumentException("Empty gameId");
            }
            CommentFragment fragment = new CommentFragment();
            Bundle args = new Bundle();
            args.putString(ARG_GAME_ID, gameId);
            fragment.setArguments(args);
            return fragment;
        }

        public CommentFragment() {
            // required empty constructor.
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mGameId = getArguments().getString(ARG_GAME_ID);

            getComments(mGameId, null);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_comment, container, false);
            ButterKnife.inject(this, rootView);

            mAdapter = new CommentAdapter(getActivity());
            mListView.setEmptyView(mEmptyView);
            mListView.setAdapter(mAdapter);

            // Sets initial view states.
            mListView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.GONE);
            registerForContextMenu(mListView);
            mLoadingView.setVisibility(View.VISIBLE);

            sendButtonSetEnabled(false);
            mCommentEditBox.setEnabled(false);
            mCommentEditBox.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // do nothing.
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // do nothing.
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        sendButtonSetEnabled(false);
                    } else {
                        if (!mSendButton.isEnabled())
                            sendButtonSetEnabled(true);
                    }
                }
            });

            TypefaceUtils.applyTypeface(mCommentEditBox, TypefaceUtils.ROBOTO_REGULAR);

            return rootView;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.reset(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            Cursor cursor = (Cursor) mAdapter.getItem(info.position);
            final String authorId = cursor.getString(cursor.getColumnIndex(CommentsEntry.COLUMN_AUTHOR_ID));
            final String userId = UserAccount.getUserAccount(getActivity()).getUserId();

            MenuInflater inflater = getActivity().getMenuInflater();
            // If selected comment is authored by current user, show user menu.
            // else show the general comment menu.
            if (TextUtils.equals(authorId, userId)) {
                inflater.inflate(R.menu.menu_comment_user, menu);
            } else {
                inflater.inflate(R.menu.menu_comment_general, menu);
            }
        }

        @Override
        public boolean onContextItemSelected(android.view.MenuItem item) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            switch (item.getItemId()) {
                case R.id.delete:
                    deleteComment(info.position);
                    return true;
                case R.id.flag:
                    flagComment(info.position);
                    return true;
                case R.id.copy:
                    copyCommentText(info.position);
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        // deletes comment with given position in the adapter.
       private void deleteComment(int position) {
           Cursor cursor = (Cursor) mAdapter.getItem(position);
           final long commentId = cursor.getLong(cursor.getColumnIndex(CommentsEntry.COLUMN_COMMENT_ID));
           LOGD(TAG, "deleteComment:: commentId:" + commentId);
           getRoboHelper().subscribe(new Action1<RoboHelper>() {
               @Override
               public void call(RoboHelper roboHelper) {
                   roboHelper.deleteComment(commentId);
               }
           });
        }

        // flags comment with given position in the adapter.
        private void flagComment(int position) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            final long commentId = cursor.getLong(cursor.getColumnIndex(CommentsEntry.COLUMN_COMMENT_ID));
            getRoboHelper().subscribe(new Action1<RoboHelper>() {
                @Override
                public void call(RoboHelper roboHelper) {
                    roboHelper.flagComment(commentId);
                }
            });
        }

        // copies comment text into system clipboard with given position in the adapter.
        private void copyCommentText(int position) {
            Cursor cursor = (Cursor) mAdapter.getItem(position);
            String comment = cursor.getString(cursor.getColumnIndex(CommentsEntry.COLUMN_MESSAGE));
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("comment", comment);
            clipboard.setPrimaryClip(clip);
        }

        // Sets send button enabled state.
        private void sendButtonSetEnabled(boolean enabled) {
            mSendButton.setEnabled(enabled);
            if (enabled)
                mSendButton.setAlpha(1.f);
            else
                mSendButton.setAlpha(0.3f);
        }

        @OnClick(R.id.sendCommentButton)
        public void sendComment() {
            if (Utils.isNetworkAvailable(getActivity())) {
                final String gameId = mGameId;
                final String message = mCommentEditBox.getText().toString();
                getRoboHelper().subscribe(new Action1<RoboHelper>() {
                    @Override
                    public void call(RoboHelper roboHelper) {
                        roboHelper.postComment(gameId, message);
                    }
                });
                mCommentEditBox.setText("");
            } else {
                ErrorUtils.showNetworkError(getActivity());
            }
        }

        // Gets comments from servers. If successful, starts loader.
        private void getComments(final String gameId, final String nextPageToken) {
            addSubscription(bindObservable(getRoboHelper().flatMap(new Func1<RoboHelper, Observable<String>>() {
                @Override
                public Observable<String> call(RoboHelper roboHelper) {
                    return roboHelper.getComments(gameId, nextPageToken);
                }
            }))
            .subscribe(new AndroidSubscriber<String>() {
                @Override
                public void onNext(String nextPageToken) {
                    LOGD(TAG, "getComments:: finished");

                    // Loads data from database.
                    mNextPageToken = nextPageToken;
                    getLoaderManager().initLoader(COMMENT_LOADER_ID, null, CommentFragment.this);
                }
            }));
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (COMMENT_LOADER_ID == id) {
                // Select comments that are not flagged for gameId.
                return new CursorLoader(getActivity(),
                        CommentsEntry.CONTENT_URI,
                        null,
                        and(
                            equal(CommentsEntry.COLUMN_GAME_ID, mGameId),
                            neq(CommentsEntry.COLUMN_FLAGGED, 1)),
                        null,
                        CommentsEntry.COLUMN_TIMESTAMP + " DESC");
            } else {
                throw new IllegalArgumentException("Unknown loader id");
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (COMMENT_LOADER_ID == loader.getId()) {

                // Updating views state.
                mCommentEditBox.setEnabled(true);
                mLoadingView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);

                mAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (COMMENT_LOADER_ID == loader.getId()) {
                mAdapter.swapCursor(null);
            }
        }
    }
}
