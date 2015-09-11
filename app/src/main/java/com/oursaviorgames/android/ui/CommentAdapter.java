package com.oursaviorgames.android.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.oursaviorgames.android.util.TypefaceUtils;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.oursaviorgames.android.R;
import com.oursaviorgames.android.data.GameContract.CommentsEntry;
import com.oursaviorgames.android.ui.drawable.PlaceHolderDrawable;
import com.oursaviorgames.android.util.DateUtils;
import com.oursaviorgames.android.util.UiUtils;

public class CommentAdapter extends CursorAdapter {

    private final Context mContext;
    private final Picasso mPicasso;
    private final MyTypefaceSpan mUsernameSpan;
    private final MyTypefaceSpan mCommentSpan;

    public CommentAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
        mPicasso = new Picasso.Builder(context).build();
        Typeface mUsernameTypeface = TypefaceUtils.getTypeface(context, TypefaceUtils.ROBOTO_MEDIUM);
        Typeface mCommentTypeface = TypefaceUtils.getTypeface(context, TypefaceUtils.ROBOTO_REGULAR);
        mUsernameSpan = new MyTypefaceSpan(mUsernameTypeface);
        mCommentSpan = new MyTypefaceSpan(mCommentTypeface);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_comment, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Gets data from cursor
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor, values);
        final String profileImageUrl = values.getAsString(CommentsEntry.COLUMN_AUTHOR_PROFILE_IMAGE);
        final String authorName = values.getAsString(CommentsEntry.COLUMN_AUTHOR_USERNAME);
        final String comment = values.getAsString(CommentsEntry.COLUMN_MESSAGE);
        final long timestamp = DateUtils.getUnixTimeFromTimestamp(values.getAsString(CommentsEntry.COLUMN_TIMESTAMP));

        ViewHolder holder = (ViewHolder) view.getTag();

        SpannableString sp1 = new SpannableString(authorName + "  ");
        sp1.setSpan(mUsernameSpan, 0, authorName.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        SpannableString sp2 = new SpannableString(comment);
        sp2.setSpan(mCommentSpan, 0, comment.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        holder.mCommentTextView.setText(TextUtils.concat(sp1, sp2));
        mPicasso.load(profileImageUrl).placeholder(holder.mPlaceHolder).into(holder.mProfileImageView);

        holder.mTimeStampTextView.setText(DateUtils.getRelativeTimeSpanString(timestamp));
    }

    /**
     * ViewHolder pattern
     */
    static class ViewHolder {

        @InjectView(R.id.profileImageView)
        ImageView mProfileImageView;
        @InjectView(R.id.commentTextView)
        TextView mCommentTextView;
        @InjectView(R.id.timestampTextView)
        TextView mTimeStampTextView;

        Drawable mPlaceHolder;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
            mPlaceHolder = new PlaceHolderDrawable(view.getContext());
        }

    }

}
