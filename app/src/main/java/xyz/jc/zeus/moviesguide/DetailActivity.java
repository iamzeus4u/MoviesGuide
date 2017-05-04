package xyz.jc.zeus.moviesguide;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import xyz.jc.zeus.moviesguide.data.MovieColumns;
import xyz.jc.zeus.moviesguide.data.MovieProvider;
import xyz.jc.zeus.moviesguide.databinding.ActivityDetailBinding;
import xyz.jc.zeus.moviesguide.utilities.DetailHelper;

public class DetailActivity extends AppCompatActivity {
    private ImageView mPoster;

    private String[] mMovieInfo;
    private String mPosterUrlString;
    private MenuItem fav;

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
                    onUnMarkFavorite();
                    fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav0));
                } else {
                    onMarkFavorite();
                    fav.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_fav1));
                }
                return true;
            case R.id.action_settings:
                Intent startSettingsActivityIntent = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivityIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void onMarkFavorite() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieColumns.ID, mMovieInfo[0]);
        contentValues.put(MovieColumns.ORIGINAL_TITLE, mMovieInfo[1]);
        contentValues.put(MovieColumns.POSTER_PATH, mMovieInfo[2]);
        contentValues.put(MovieColumns.MOVIES_OVERVIEW, mMovieInfo[3]);
        contentValues.put(MovieColumns.RELEASE_DATE, mMovieInfo[4]);
        contentValues.put(MovieColumns.USER_RATING, mMovieInfo[5]);
        Uri uri = getContentResolver().insert(MovieProvider.Movies.CONTENT_URI, contentValues);
        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        if (uri != null) {
            Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void onUnMarkFavorite() {
        getContentResolver().delete(MovieProvider.Movies.withId(mMovieInfo[0]), null, null);
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