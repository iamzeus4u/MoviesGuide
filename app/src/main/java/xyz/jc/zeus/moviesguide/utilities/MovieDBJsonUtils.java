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
package xyz.jc.zeus.moviesguide.utilities;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import xyz.jc.zeus.moviesguide.MovieReview;
import xyz.jc.zeus.moviesguide.MovieVideo;

/**
 * Utility functions to handle MoviesDb JSON data.
 */
public final class MovieDBJsonUtils {
    /**
     * @param moviesJsonStr JSON response from server
     * @return Array of Strings describing movie data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static String[][] getMoviesInfoStringsFromJson(Context context, String moviesJsonStr)
            throws JSONException {
        final String MOVIE_DATA = "results";
        final String ID = "id";
        final String ORIGINAL_TITLE = "original_title";
        final String POSTER_PATH = "poster_path";
        final String MOVIES_OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String USER_RATING = "vote_average";
        final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w342";

        /* String array to hold each movie information String
        * id = 0
        * original_title = 1
        * poster_path = 2
        * overview = 3
        * release_date = 4
        * vote_average = 5
        * */
        String[][] movieInfo = null;
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray movieData = moviesJson.getJSONArray(MOVIE_DATA);
        movieInfo = new String[movieData.length()][];

        for (int i = 0; i < movieData.length(); i++) {
            JSONObject movieInfoJson = movieData.getJSONObject(i);
            movieInfo[i] = new String[6];
            movieInfo[i][0] = movieInfoJson.getString(ID);
            movieInfo[i][1] = movieInfoJson.getString(ORIGINAL_TITLE);
            movieInfo[i][2] = POSTER_BASE_URL + movieInfoJson.getString(POSTER_PATH);
            movieInfo[i][3] = movieInfoJson.getString(MOVIES_OVERVIEW);
            movieInfo[i][4] = movieInfoJson.getString(RELEASE_DATE);
            movieInfo[i][5] = movieInfoJson.getString(USER_RATING);
        }
        return movieInfo;
    }

    public static List<MovieReview> getMoviesReviewsStringsFromJson(String reviewJsonStr)
            throws JSONException {
        final String MOVIE_DATA = "results";
        JSONObject reviewJson = new JSONObject(reviewJsonStr);
        JSONArray reviewData = reviewJson.getJSONArray(MOVIE_DATA);
        return MovieReview.fromJson(reviewData);
    }

    public static List<MovieVideo> getMoviesVideosStringsFromJson(String videosJsonStr)
            throws JSONException {
        final String MOVIE_DATA = "results";

        JSONObject videosJson = new JSONObject(videosJsonStr);
        JSONArray videosData = videosJson.getJSONArray(MOVIE_DATA);
        return MovieVideo.fromJson(videosData);
    }
}