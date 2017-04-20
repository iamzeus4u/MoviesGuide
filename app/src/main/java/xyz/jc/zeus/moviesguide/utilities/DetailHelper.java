package xyz.jc.zeus.moviesguide.utilities;

import android.content.Context;

import xyz.jc.zeus.moviesguide.R;

/**
 * Created by zeus on 19/04/2017.
 */

public class DetailHelper {
    public final String mTitle;
    public final String mRating;
    public final String mDate;
    public final String mOverview;

    public DetailHelper(Context context, String mTitle, String mOverview, String mDate, String mRating) {
        this.mTitle = mTitle;
        this.mRating = context.getString(R.string.user_rating) + mRating;
        this.mDate = mDate;
        this.mOverview = mOverview;
    }
}
