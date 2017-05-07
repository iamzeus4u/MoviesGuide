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

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import xyz.jc.zeus.moviesguide.data.ApiKeys;

/**
 * These utilities will be used to communicate with the servers.
 */
public final class NetworkUtils {
    private static final String TAG = xyz.jc.zeus.moviesguide.utilities.NetworkUtils.class.getSimpleName();
    private static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie";

    /* The lang we want our API to return */
    private static final String api = ApiKeys.MOVIEDB_API_KEY;
    private static final String lang = "en-US";

    /* The number of pages we want our API to return */
    private static final int pages = 10;
    private final static String QUERY_API_PARAM = "api_key";
    private final static String LANG_PARAM = "language";
    private final static String PAGE_PARAM = "page";

    /**
     * Builds the URL used to talk to the movies server using a sort Parameter.
     *
     * @param sortByQuery The sort param that will be queried for.
     * @return The Array of URL to use to query the movie server.
     */
    public static URL[] buildUrl(String sortByQuery) {
        Uri[] builtUri = new Uri[pages];
        URL[] url = new URL[pages];
        for (int page = 1; page <= pages; page++) {
            builtUri[page - 1] = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(sortByQuery)
                    .appendQueryParameter(QUERY_API_PARAM, api)
                    .appendQueryParameter(LANG_PARAM, lang)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                    .build();
            url[page - 1] = null;
            try {
                url[page - 1] = new URL(builtUri[page - 1].toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Built URI " + url[page - 1]);
        }
        return url;
    }

    public static URL buildUrlForDetail(String id, String data) {
        Uri builtUri;
        URL url;
        builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                .appendPath(id)
                .appendPath(data)
                .appendQueryParameter(QUERY_API_PARAM, api)
                .build();
        url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Built URI " + url);
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}