package xyz.jc.zeus.moviesguide;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import xyz.jc.zeus.moviesguide.data.MovieColumns;
import xyz.jc.zeus.moviesguide.data.MovieProvider;
import xyz.jc.zeus.moviesguide.databinding.ActivityDetailBinding;
import xyz.jc.zeus.moviesguide.utilities.DetailHelper;
import xyz.jc.zeus.moviesguide.utilities.MovieDBJsonUtils;
import xyz.jc.zeus.moviesguide.utilities.NetworkUtils;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MovieReview>> {
    private ImageView mPoster;
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private String jsonMovieReviewResponse;
    private String[] mMovieInfo;
    private String mPosterUrlString;
    private MenuItem fav;
    private static final int MOVIE_LOADER_ID = 0;
    private List<MovieReview> mReviewInfo;
    private ExpandableHeightListView reviewListView;
    private ReviewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
        mPoster = (ImageView) findViewById(R.id.movie_detail_poster);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mMovieInfo = intentThatStartedThisActivity.getStringArrayExtra("movie");
                mPosterUrlString = mMovieInfo[2].replace("w342", "w780");
                Context context = this;
                Picasso.with(context).load(mPosterUrlString).placeholder(R.drawable.poster_placeholder).into(mPoster);

                DetailHelper detailHelper = new DetailHelper(context, mMovieInfo[1], mMovieInfo[3], mMovieInfo[4], mMovieInfo[5]);
                binding.setDetail(detailHelper);
            }
        }
        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, DetailActivity.this);

        // Attach the adapter to a ListView
        reviewListView = (ExpandableHeightListView) findViewById(R.id.review_listView);
        reviewListView.setExpanded(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, DetailActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail, menu);
        fav = menu.findItem(R.id.action_favorite);
        if (isFAVOURITE(mMovieInfo[0])) {
            fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav1));
        } else {
            fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav0));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_favorite:
                if (isFAVOURITE(mMovieInfo[0])) {
                    if (onUnMarkFavorite()) {
                        fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav0));
                    }
                } else {
                    if (onMarkFavorite()) {
                        fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav1));
                    }
                }
                return true;
            case R.id.action_settings:
                Intent startSettingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivityIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onMarkFavorite() {
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(MovieColumns.ID, mMovieInfo[0]);
        mContentValues.put(MovieColumns.ORIGINAL_TITLE, mMovieInfo[1]);
        mContentValues.put(MovieColumns.POSTER_PATH, mMovieInfo[2]);
        mContentValues.put(MovieColumns.MOVIES_OVERVIEW, mMovieInfo[3]);
        mContentValues.put(MovieColumns.RELEASE_DATE, mMovieInfo[4]);
        mContentValues.put(MovieColumns.USER_RATING, mMovieInfo[5]);
        mContentValues.put(MovieColumns.JSON_REVIEW, jsonMovieReviewResponse);

        try {
            Uri uri = getContentResolver().insert(MovieProvider.Movies.CONTENT_URI, mContentValues);
            if (uri != null) {
                Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
            }
            return true;
        } catch (SQLiteConstraintException e) {
            Toast.makeText(this, "Wait for info to laod and try again!", Toast.LENGTH_LONG).show();
            return false;
        }

    }

    private boolean onUnMarkFavorite() {
        try {
            getContentResolver().delete(MovieProvider.Movies.withId(mMovieInfo[0]), null, null);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private boolean isFAVOURITE(String id) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(MovieProvider.Movies.CONTENT_URI, null, null, null, null);
        boolean FAVOURITE;
        if (cursor != null && cursor.getCount() > 0) {
            List<String> mID = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                mID.add(cursor.getString(cursor.getColumnIndex(MovieColumns.ID)));
                cursor.moveToNext();
            }
            cursor.close();
            FAVOURITE = mID.contains(id);
        } else {
            FAVOURITE = false;
        }
        return FAVOURITE;
    }

    @Override
    public Loader<List<MovieReview>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<MovieReview>>(this) {
            List<MovieReview> moviesExInfo = null;

            @Override
            protected void onStartLoading() {
                if (moviesExInfo != null) {
                    deliverResult(moviesExInfo);
                } else {
                    //mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<MovieReview> loadInBackground() {
                URL movieReviewRequestUrl = NetworkUtils.buildUrlForDetail(mMovieInfo[0], "reviews");
                List<MovieReview> reviews = null;

                try {
                    if (!isFAVOURITE(mMovieInfo[0])) {
                        //Iterating through the array of URL received from NetworkUtils.buildUrl()
                        jsonMovieReviewResponse = NetworkUtils.getResponseFromHttpUrl(movieReviewRequestUrl);
                        reviews = MovieDBJsonUtils.getMoviesReviewsStringsFromJson(jsonMovieReviewResponse);
                    } else {
                        ContentResolver resolver = getContentResolver();
                        Cursor cursor = resolver.query(MovieProvider.Movies.CONTENT_URI, null, null, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                jsonMovieReviewResponse = cursor.getString(cursor.getColumnIndex(MovieColumns.JSON_REVIEW));
                                reviews = MovieDBJsonUtils.getMoviesReviewsStringsFromJson(jsonMovieReviewResponse);
                                cursor.moveToNext();
                            }
                            cursor.close();
                        }

                    }
                    return reviews;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(List<MovieReview> data) {
                moviesExInfo = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<MovieReview>> loader, List<MovieReview> data) {
        //mLoadingIndicator.setVisibility(View.INVISIBLE);
        mReviewInfo = data;
        if (data != null) {
            //showPosterDataView();
            adapter = new ReviewAdapter(this, mReviewInfo);
            reviewListView.setAdapter(adapter);

        } else {
            //showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<MovieReview>> loader) {

    }
}