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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.jc.zeus.moviesguide.MoviesAdapter.MoviesAdapterOnClickHandler;
import xyz.jc.zeus.moviesguide.utilities.MovieDBJsonUtils;
import xyz.jc.zeus.moviesguide.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity implements MoviesAdapterOnClickHandler {

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

        /* This String is used to store the sort parameter and by default set to "popular" */
        sortBy = getString(R.string.pick_popular);


        GridLayoutManager layoutManager
                = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);

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
         *
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the movies data. */
        loadMovieData(sortBy);
    }


    private void loadMovieData(String sortBy) {
        showPosterDataView();

        if (sortBy == getString(R.string.pick_popular)) {
            this.setTitle(getString(R.string.app_name) + ": sorted by popularity");
        } else if (sortBy == getString(R.string.pick_rated)) {
            this.setTitle(getString(R.string.app_name) + ": sorted by user rating");
        } else {
            this.setTitle(getString(R.string.app_name));
        }
        new FetchMovieDataTask().execute(sortBy);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param selectedMovie The movie poster that was clicked
     */
    @Override
    public void onClick(int selectedMovie) {
        Context context = this;
        Class destinationClass = xyz.jc.zeus.moviesguide.DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie",mMoviesInfo[selectedMovie]);
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

    public class FetchMovieDataTask extends AsyncTask<String, Void, String[][]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[][] doInBackground(String... params) {

            /* If there's no zip code, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String category = params[0];
            URL[] movieDbRequestUrl = NetworkUtils.buildUrl(category);
            List<String[]> tempMovieInfo = new ArrayList<>();


            try {
                /*Iterating through the array of URL received from NetworkUtils.buildUrl()

                 */
                for (URL aMovieDbRequestUrl : movieDbRequestUrl) {
                    String jsonMovieDbResponse = NetworkUtils
                            .getResponseFromHttpUrl(aMovieDbRequestUrl);

                    String[][] tmpMInfo = MovieDBJsonUtils
                            .getMoviesInfoStringsFromJson(MainActivity.this, jsonMovieDbResponse);

                    if (tmpMInfo != null) {
                        Collections.addAll(tempMovieInfo, tmpMInfo);
                    }

                }

                String[][] moviesInfo =  new String[tempMovieInfo.size()][];
                moviesInfo = tempMovieInfo.toArray(moviesInfo);

                return moviesInfo;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[][] moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mMoviesInfo = moviesData;

            if (moviesData != null) {
                showPosterDataView();
                mMoviesAdapter.setPosterData(moviesData);
            } else {
                showErrorMessage();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.moviedb, menu);

        return true;
    }

    public Dialog sortCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.pick_sort)
                .setItems(R.array.sort_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      if (which == 1){
                          sortBy =getString(R.string.pick_rated);
                      }else {
                          sortBy =getString(R.string.pick_popular);
                      }

                      mMoviesAdapter.setPosterData(null);
                      loadMovieData(sortBy);
                    }
                });
        return builder.create();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id){
            case R.id.action_refresh:
                mMoviesAdapter.setPosterData(null);
                loadMovieData(sortBy);
                return true;

            case R.id.action_sort:
                sortCreateDialog().show();


        }

        return super.onOptionsItemSelected(item);
    }
}