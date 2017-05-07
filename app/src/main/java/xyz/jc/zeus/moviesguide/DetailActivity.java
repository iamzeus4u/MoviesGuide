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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightListView;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import xyz.jc.zeus.moviesguide.data.ApiKeys;
import xyz.jc.zeus.moviesguide.data.MovieColumns;
import xyz.jc.zeus.moviesguide.data.MovieProvider;
import xyz.jc.zeus.moviesguide.databinding.ActivityDetailBinding;
import xyz.jc.zeus.moviesguide.utilities.DetailHelper;
import xyz.jc.zeus.moviesguide.utilities.MovieDBJsonUtils;
import xyz.jc.zeus.moviesguide.utilities.NetworkUtils;

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int REVIEW_LOADER_ID = 0;
    private static final int VIDEO_LOADER_ID = 1;
    private ImageView mPoster;
    private String jsonMovieReviewResponse;
    private String[] mMovieInfo;
    private String mPosterUrlString;
    private MenuItem fav;
    private List<MovieReview> mReviewInfo;
    private List<MovieVideo> mVideoInfo;
    private ExpandableHeightListView reviewListView;
    private MovieReviewAdapter reviewAdapter;
    private MovieVideoAdapter videoAdapter;
    private String jsonMovieVideoResponse;
    private LoaderManager.LoaderCallbacks<List<MovieReview>> reviewResultLoaderListener;
    private LoaderManager.LoaderCallbacks<List<MovieVideo>> videoResultLoaderListener;
    private ExpandableHeightListView videoListView;
    private boolean loadersInitState = false;
    private TextView vtextView;
    private TextView rtextView;
    private String YOUTUBE_API_KEY = ApiKeys.YOUTUBE_API_KEY;

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
        reviewResultLoaderListener = new LoaderManager.LoaderCallbacks<List<MovieReview>>() {
            @Override
            public Loader<List<MovieReview>> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<List<MovieReview>>(DetailActivity.this) {
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
                        List<MovieReview> reviews = null;

                        try {
                            if (!isFAVOURITE(mMovieInfo[0])) {
                                URL movieReviewRequestUrl = NetworkUtils.buildUrlForDetail(mMovieInfo[0], "reviews");
                                jsonMovieReviewResponse = NetworkUtils.getResponseFromHttpUrl(movieReviewRequestUrl);
                                reviews = MovieDBJsonUtils.getMoviesReviewsStringsFromJson(jsonMovieReviewResponse);
                            } else {
                                ContentResolver resolver = getContentResolver();
                                Cursor cursor = resolver.query(MovieProvider.Movies.withId(mMovieInfo[0]), null, null, null, null);
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
                reviewAdapter = new MovieReviewAdapter(DetailActivity.this, mReviewInfo);
                reviewListView.setAdapter(reviewAdapter);

                if (data != null & data.size() != 0) {
                    //showPosterDataView();
                    rtextView.setText("REVIEWS");


                } else {
                    //showErrorMessage();
                    rtextView.setText("NO REVIEWS");

                }
            }

            @Override
            public void onLoaderReset(Loader<List<MovieReview>> loader) {

            }
        };
        videoResultLoaderListener = new LoaderManager.LoaderCallbacks<List<MovieVideo>>() {
            @Override
            public Loader<List<MovieVideo>> onCreateLoader(int id, Bundle args) {
                return new AsyncTaskLoader<List<MovieVideo>>(DetailActivity.this) {
                    List<MovieVideo> moviesVideoInfo = null;

                    @Override
                    protected void onStartLoading() {
                        if (moviesVideoInfo != null) {
                            deliverResult(moviesVideoInfo);
                        } else {
                            //mLoadingIndicator.setVisibility(View.VISIBLE);
                            forceLoad();
                        }
                    }

                    @Override
                    public List<MovieVideo> loadInBackground() {
                        List<MovieVideo> videos = null;

                        try {
                            if (!isFAVOURITE(mMovieInfo[0])) {
                                URL movieVideoRequestUrl = NetworkUtils.buildUrlForDetail(mMovieInfo[0], "videos");
                                jsonMovieVideoResponse = NetworkUtils.getResponseFromHttpUrl(movieVideoRequestUrl);
                                videos = MovieDBJsonUtils.getMoviesVideosStringsFromJson(jsonMovieVideoResponse);
                            } else {
                                ContentResolver resolver = getContentResolver();
                                Cursor cursor = resolver.query(MovieProvider.Movies.withId(mMovieInfo[0]), null, null, null, null);
                                if (cursor != null && cursor.getCount() > 0) {
                                    cursor.moveToFirst();
                                    while (!cursor.isAfterLast()) {
                                        jsonMovieVideoResponse = cursor.getString(cursor.getColumnIndex(MovieColumns.JSON_VIDEO));
                                        videos = MovieDBJsonUtils.getMoviesVideosStringsFromJson(jsonMovieVideoResponse);
                                        cursor.moveToNext();
                                    }
                                    cursor.close();
                                }
                            }
                            return videos;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    public void deliverResult(List<MovieVideo> data) {
                        moviesVideoInfo = data;
                        super.deliverResult(data);
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<List<MovieVideo>> loader, List<MovieVideo> data) {
                //mLoadingIndicator.setVisibility(View.INVISIBLE);
                mVideoInfo = data;
                videoAdapter = new MovieVideoAdapter(getApplicationContext(), mVideoInfo);
                videoListView.setAdapter(videoAdapter);

                if (data != null & data.size() != 0) {
                    //showPosterDataView();
                    vtextView.setText("VIDEOS");


                } else {
                    //showErrorMessage();
                    vtextView.setText("NO VIDEOS");

                }
            }

            @Override
            public void onLoaderReset(Loader<List<MovieVideo>> loader) {

            }
        };

        reviewListView = (ExpandableHeightListView) findViewById(R.id.review_listView);
        reviewListView.setExpanded(true);
        LayoutInflater rinflater = LayoutInflater.from(DetailActivity.this);
        View rconvertView = rinflater.inflate(R.layout.listview_header, null, false);
        rtextView = ((TextView) rconvertView.findViewById(R.id.listView_header));
        reviewListView.addHeaderView(rconvertView);

        videoListView = (ExpandableHeightListView) findViewById(R.id.video_listView);
        videoListView.setExpanded(true);
        LayoutInflater vinflater = LayoutInflater.from(getApplicationContext());
        View vconvertView = vinflater.inflate(R.layout.listview_header, null, false);
        vtextView = ((TextView) vconvertView.findViewById(R.id.listView_header));
        vtextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<String> keys = new ArrayList<String>();
                    for (MovieVideo video : mVideoInfo) {
                        keys.add(video.getKEY);
                    }
                    Intent intent = YouTubeStandalonePlayer.createVideosIntent(DetailActivity.this, YOUTUBE_API_KEY, keys, 0, 0, true, true);
                    startActivity(intent);
                } catch (Exception e) {
                            /*startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mVideoInfo.get(position).getURL)));
                            Log.i("Video", "Video Playing" + mVideoInfo.get(position).getURL);*/
                }
            }
        });
        videoListView.addHeaderView(vconvertView);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent intent = YouTubeStandalonePlayer.createVideoIntent(DetailActivity.this, YOUTUBE_API_KEY, mVideoInfo.get(position - 1).getKEY, 0, true, true);
                    startActivity(intent);
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mVideoInfo.get(position).getURL)));
                    Log.i("Video", "Video Playing" + mVideoInfo.get(position).getURL);
                }
            }
        });
        if (savedInstanceState != null) {
            loadersInitState = savedInstanceState.getBoolean("loadersInitState", false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loadersInitState", loadersInitState);
    }

    private void manageLoaders(boolean INITIALIZED) {
        if (INITIALIZED) {
            getSupportLoaderManager().restartLoader(REVIEW_LOADER_ID, null, reviewResultLoaderListener);
            getSupportLoaderManager().restartLoader(VIDEO_LOADER_ID, null, videoResultLoaderListener);
        } else {
            getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, null, reviewResultLoaderListener);
            getSupportLoaderManager().initLoader(VIDEO_LOADER_ID, null, videoResultLoaderListener);
            loadersInitState = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        manageLoaders(loadersInitState);
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
        mContentValues.put(MovieColumns.JSON_VIDEO, jsonMovieVideoResponse);

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

}