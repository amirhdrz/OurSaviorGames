package com.oursaviorgames.android.ui.widget;

import android.content.Context;
import android.support.v7.internal.widget.TintEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;

import java.util.regex.Pattern;

import com.oursaviorgames.android.R;
import com.oursaviorgames.android.util.ErrorUtils;

import static com.oursaviorgames.android.util.LogUtils.LOGD;
import static com.oursaviorgames.android.util.LogUtils.makeLogTag;

/**
 * TextView that also verifies typed in username.
 * Shows appropriate error message as Toasts.
 */
public class UsernameEditText extends TintEditText {

    private static final String TAG = makeLogTag(UsernameEditText.class);

    //TODO: channge max length on the server too
    //TODO: auto generated names cannot be longer than this length. guarantee this on the server.
    // Maximum number of chars that can be entered.
    private static final int MAX_LENGTH         = 22;
    // Padding on the left and right side of the view.
    private static final int HORIZONTAL_PADDING = 8;

    // Legal characters as a regex
    private Pattern mLegalChars = Pattern.compile("_|\\.|([a-zA-Z0-9])");

    public UsernameEditText(Context context) {
        super(context);
        init();
    }

    public UsernameEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UsernameEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Due to an Android bug we have to set the padding from here.
        int padding = (int) convertDipToPx(HORIZONTAL_PADDING);
        setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
        // set all the ui behaviour here.
        setSingleLine();
        setMaxLines(1);
        // Sets a LengthFilter and a text input filter.
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LENGTH), buildInputFilter()});
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore.
            }

            @Override
            public void afterTextChanged(Editable s) {
                LOGD(TAG, "afterTextChanged: " + s.toString());

            }
        });
    }

    /**
     * Returns a validated string from the TextView.
     * @return validated username string or null.
     */
    public String getValidatedString() {
        String s = getText().toString();
        //TODO: should match server
        if (s.length() < 5) {
            ErrorUtils.showShortTopError(getContext(), R.string.error_username_too_short);
        } else if (s.length() <= MAX_LENGTH) {
            if (!s.matches("(.*\\.$)")) {
                if (validateAllowIllegalEnding(s)) {
                    return s;
                }
            } else {
                // Username can't end with period.
                ErrorUtils.showShortTopError(getContext(), R.string.error_username_end_with_period);
            }
        }
        return null;
    }

    /**
     * @param s Partial or complete username, allows for the last
     *          to be any char that would have otherwise been valid
     *          if it were in the middle of the String s, but not
     *          necessarily valid at the end of the String s.
     * @return
     */
    private boolean validateAllowIllegalEnding(String s) {
        // Don't validate empty strings.
        if (s.length() == 0) {
            return true;
        }
        // Username can't start with a period.
        if (s.matches("(^\\..*)")) {
            ErrorUtils.showShortTopError(getContext(), R.string.error_username_start_with_period);
        }
        // Can't have multiple periods or underscores in a row
        if (s.contains("..") || s.contains("__") || s.contains("._") || s.contains("_.")) {
            ErrorUtils.showShortTopError(getContext(), R.string.error_username_multiple_period_underscore);
        }
        // Checks if all the characters are legal;
        for (int i = 0; i < s.length(); i++) {
            if (!mLegalChars.matcher(String.valueOf(s.charAt(i))).matches()) {
                ErrorUtils.showShortTopError(getContext(), R.string.error_username_illegal_char);
            }
        }
        // Actual validation is done here.
        if (!s.matches("(^\\..*)")) {
            if (s.matches("^_?([a-zA-Z0-9][_\\.]?)+")) {
                return true;
            }
        }
        return false;
    }


    private float convertDipToPx(float dips) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, getResources().getDisplayMetrics());
    }

    private InputFilter buildInputFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                LOGD(TAG, "Source: " + source);
                LOGD(TAG, "Dest: " + dest);
                LOGD(TAG, String.format("start: %d, end: %d, dstart: %d, dend: %d", start, end, dstart, dend));

                for (int i = start; i < end; i++) {
                    char[] v = new char[dend + (dend - dstart) + (i + 1 - start)];
                    TextUtils.getChars(dest, 0, dstart, v, 0);
                    TextUtils.getChars(source, start, i + 1, v, dstart);
                    String s = new String(v);
                    LOGD(TAG, "String s: " + s);
                    if (!validateAllowIllegalEnding(s)) {
                        if (source instanceof Spanned) {
                            SpannableString sp = new SpannableString(source.subSequence(start, i));
                            TextUtils.copySpansFrom((Spanned) source, start, i, null, sp, 0);
                            return sp;
                        }
                        return source.subSequence(start, i);
                    }
                }

                return null; // keep original
            }
        };
    }

}
