/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.jc.zeus.moviesguide;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.jc.zeus.moviesguide.MoviesAdapter.MoviesAdapterOnClickHandler;
import xyz.jc.zeus.moviesguide.data.MovieColumns;
import xyz.jc.zeus.moviesguide.data.MovieProvider;
import xyz.jc.zeus.moviesguide.utilities.MovieDBJsonUtils;
import xyz.jc.zeus.moviesguide.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements MoviesAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener, LoaderCallbacks<String[][]> {

    private static final int MOVIE_LOADER_ID = 0;
    private static boolean MAIN_PREFERENCES_UPDATED = false;
    private RecyclerView mRecyclerView;
    private MoviesAdapter mMoviesAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private String[][] mMoviesInfo;
    private String sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.error_message_display);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        /*
         * The MoviesAdapter is responsible for linking our MovieData with the Views that
         * will end up displaying our movie data.
         */
        mMoviesAdapter = new MoviesAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMoviesAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the movies data. */
        loadMovieData();

        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, MainActivity.this);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (MAIN_PREFERENCES_UPDATED | sortBy.equals(getString(R.string.pref_sort_favourite))) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            sortBy = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_popularity));
            loadMovieData();
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
            MAIN_PREFERENCES_UPDATED = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle SavedInstanceState) {
        super.onSaveInstanceState(SavedInstanceState);
        SavedInstanceState.putString("sortby", sortBy);
    }

    private void loadMovieData() {
        showPosterDataView();
        invalidateData();
        Toast sortedToast;
        if (sortBy.equals(getString(R.string.pref_sort_popularity))) {
            sortedToast = Toast.makeText(this, getString(R.string.toast_popularity), Toast.LENGTH_LONG);
            sortedToast.show();
        } else if (sortBy.equals(getString(R.string.pref_sort_mostrated))) {
            sortedToast = Toast.makeText(this, getString(R.string.toast_mostrated), Toast.LENGTH_LONG);
            sortedToast.show();
        } else {
            sortedToast = Toast.makeText(this, getString(R.string.toast_favourite), Toast.LENGTH_LONG);
            sortedToast.show();
        }
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.     *
     *
     * @param selectedMovie The movie poster that was clicked
     */
    @Override
    public void onClick(int selectedMovie) {
        Context context = this;
        Class destinationClass = xyz.jc.zeus.moviesguide.DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", mMoviesInfo[selectedMovie]);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the movie posters visible and
     * hide the error message.
     */

    private void showPosterDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     */
    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void invalidateData() {
        mMoviesAdapter.setPosterData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                loadMovieData();
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, MainActivity.this);
                return true;
            case R.id.action_settings:
                Intent startSettingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivityIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<String[][]> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String[][]>(this) {
            String[][] moviesInfo = null;

            @Override
            protected void onStartLoading() {
                if (moviesInfo != null) {
                    deliverResult(moviesInfo);
                } else {
                    mLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public String[][] loadInBackground() {
                String category = sortBy;
                URL[] movieDbRequestUrl = NetworkUtils.buildUrl(category);
                List<String[]> tempMovieInfo = new ArrayList<>();
                try {
                    if (!sortBy.equals(getString(R.string.pref_sort_favourite))) {
                        //Iterating through the array of URL received from NetworkUtils.buildUrl()
                        for (URL aMovieDbRequestUrl : movieDbRequestUrl) {
                            String jsonMovieDbResponse = NetworkUtils.getResponseFromHttpUrl(aMovieDbRequestUrl);
                            String[][] tmpMInfo = MovieDBJsonUtils.getMoviesInfoStringsFromJson(MainActivity.this, jsonMovieDbResponse);
                            if (tmpMInfo != null) {
                                Collections.addAll(tempMovieInfo, tmpMInfo);
                            }
                        }
                    } else {
                        ContentResolver resolver = getContentResolver();
                        Cursor cursor = resolver.query(MovieProvider.Movies.CONTENT_URI, null, null, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            String[][] tmpMInfo = new String[cursor.getCount()][6];
                            cursor.moveToFirst();
                            while (!cursor.isAfterLast()) {
                                tmpMInfo[cursor.getPosition()][0] = cursor.getString(cursor.getColumnIndex(MovieColumns.ID));
                                tmpMInfo[cursor.getPosition()][1] = cursor.getString(cursor.getColumnIndex(MovieColumns.ORIGINAL_TITLE));
                                tmpMInfo[cursor.getPosition()][2] = cursor.getString(cursor.getColumnIndex(MovieColumns.POSTER_PATH));
                                tmpMInfo[cursor.getPosition()][3] = cursor.getString(cursor.getColumnIndex(MovieColumns.MOVIES_OVERVIEW));
                                tmpMInfo[cursor.getPosition()][4] = cursor.getString(cursor.getColumnIndex(MovieColumns.RELEASE_DATE));
                                tmpMInfo[cursor.getPosition()][5] = cursor.getString(cursor.getColumnIndex(MovieColumns.USER_RATING));
                                cursor.moveToNext();
                            }
                            Collections.addAll(tempMovieInfo, tmpMInfo);
                        }
                        cursor.close();
                    }
                    moviesInfo = new String[tempMovieInfo.size()][];
                    moviesInfo = tempMovieInfo.toArray(moviesInfo);
                    return moviesInfo;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(String[][] data) {
                moviesInfo = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[][]> loader, String[][] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mMoviesInfo = data;
        if (data != null) {
            showPosterDataView();
            mMoviesAdapter.setPosterData(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[][]> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        MAIN_PREFERENCES_UPDATED = true;
    }
}