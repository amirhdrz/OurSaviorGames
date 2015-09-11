package com.oursaviorgames.android.data.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;


import java.util.List;

import com.oursaviorgames.android.data.BaseGameColumns;
import com.oursaviorgames.android.data.DataUtils;

import static com.oursaviorgames.android.util.Preconditions.checkNotNull;

/**
 * An object model of {@link com.oursaviorgames.android.data.BaseGameColumns}.
 * <p>
 * Note this class makes a copy of passed in ContentValues.
 */
public class BaseGameModel implements Parcelable {

    private static final List<String> BASE_GAME_COLUMNS = DataUtils.getColumns(BaseGameColumns.class);

    private final ContentValues baseGameContentValues;

    public BaseGameModel(ContentValues cv) {
        checkNotNull(cv, "Null content values");
        baseGameContentValues = new ContentValues(BASE_GAME_COLUMNS.size());
        DataUtils.copyContentValues(cv, baseGameContentValues, BASE_GAME_COLUMNS);
    }

    public ContentValues copyContentValues() {
        return new ContentValues(baseGameContentValues);
    }

    /**
     * Returns internal base game ContentValues.
     * <p>
     * <b> Client should guarantee to not write any values to returned object.</b>
     */
    public ContentValues getContentValues() {
        return baseGameContentValues;
    }

    public String getGameId() {
        return baseGameContentValues.getAsString(BaseGameColumns.COLUMN_GAME_ID);
    }

    public String getDeveloperId() {
        return baseGameContentValues.getAsString(BaseGameColumns.COLUMN_DEVELOPER_ID);
    }

    public String getGameTitle() {
        return baseGameContentValues.getAsString(BaseGameColumns.COLUMN_GAME_TITLE);
    }

    public String getDeveloperName() {
        return baseGameContentValues.getAsString(BaseGameColumns.COLUMN_DEVELOPER_NAME);
    }

    public Integer getPlayCount() {
        return baseGameContentValues.getAsInteger(BaseGameColumns.COLUMN_PLAY_COUNT);
    }

    public String getOriginUrl() {
        return baseGameContentValues.getAsString(BaseGameColumns.COLUMN_ORIGIN_URL);
    }

    public Boolean isAvailableOffline() {
        return baseGameContentValues.getAsBoolean(BaseGameColumns.COLUMN_OFFLINE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.baseGameContentValues, 0);
    }

    private BaseGameModel(Parcel in) {
        this.baseGameContentValues = in.readParcelable(ContentValues.class.getClassLoader());
    }

    public static final Parcelable.Creator<BaseGameModel> CREATOR = new Parcelable.Creator<BaseGameModel>() {
        public BaseGameModel createFromParcel(Parcel source) {
            return new BaseGameModel(source);
        }

        public BaseGameModel[] newArray(int size) {
            return new BaseGameModel[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof BaseGameModel) {
            BaseGameModel model = (BaseGameModel) o;
            return this.baseGameContentValues.equals(model.baseGameContentValues);
        }
        return false;
    }
}

