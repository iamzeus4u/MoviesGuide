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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import xyz.jc.zeus.moviesguide.data.MovieColumns;
import xyz.jc.zeus.moviesguide.data.MovieProvider;

/**
 * {@link MoviesAdapter} exposes a list of movie information to a
 * {@link RecyclerView}
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MoviesAdapterOnClickHandler mClickHandler;
    private String[][] mMoviesData;
    private byte[][] favMoviesPoster;
    private Context context;
    /**
     * Creates a MoviesAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new MoviesAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movies_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MoviesAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the weather
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param moviesAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                contents of the item at the given position in the data set.
     * @param position                The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {

        String movieTitle = mMoviesData[position][1];
        String movieRating = mMoviesData[position][5];
        String moviePoster = mMoviesData[position][2];
        moviesAdapterViewHolder.mMovieTitleTextView.setText(movieTitle);
        moviesAdapterViewHolder.mMovieRatingTextView.setText(movieRating);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_popularity)).equals(context.getString(R.string.pref_sort_favourite))) {
            Bitmap bm = BitmapFactory.decodeByteArray(favMoviesPoster[position], 0, favMoviesPoster[position].length);
            moviesAdapterViewHolder.mMoviePosterImageView.setImageBitmap(bm);
        } else {
            Picasso.with(moviesAdapterViewHolder.itemView.getContext()).load(moviePoster).placeholder(R.drawable.poster_placeholder).into(moviesAdapterViewHolder.mMoviePosterImageView);
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our main
     */
    @Override
    public int getItemCount() {
        if (null == mMoviesData) return 0;
        return mMoviesData.length;
    }

    /**
     * This method is used to set the movies information on a MoviesAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MoviesAdapter to display it.
     *
     * @param moviesData The new movies data to be displayed.
     */
    public void setPosterData(String[][] moviesData, Context context) {
        mMoviesData = moviesData;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MovieProvider.Movies.CONTENT_URI, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            favMoviesPoster = new byte[cursor.getCount()][];
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                favMoviesPoster[cursor.getPosition()] = cursor.getBlob(cursor.getColumnIndex(MovieColumns.BYTES_MAIN_POSTER));
                cursor.moveToNext();
            }
            cursor.close();
        }
        notifyDataSetChanged();
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MoviesAdapterOnClickHandler {
        void onClick(int selectedMovie, byte[] selectedMoviePoster);
    }

    /**
     * Cache of the children views for a main list item.
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
        public final TextView mMovieTitleTextView;
        public final TextView mMovieRatingTextView;
        public final ImageView mMoviePosterImageView;
        public byte[] mMoviePosterImageBytes;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mMovieTitleTextView = (TextView) view.findViewById(R.id.movie_title);
            mMovieRatingTextView = (TextView) view.findViewById(R.id.movie_rating);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.movie_poster);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Bitmap bitmap = ((BitmapDrawable) mMoviePosterImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            mMoviePosterImageBytes = baos.toByteArray();
            mClickHandler.onClick(adapterPosition, mMoviePosterImageBytes);
        }
    }
}