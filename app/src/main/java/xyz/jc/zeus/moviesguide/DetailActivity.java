package xyz.jc.zeus.moviesguide;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import xyz.jc.zeus.moviesguide.databinding.ActivityDetailBinding;
import xyz.jc.zeus.moviesguide.utilities.DetailHelper;

public class DetailActivity extends AppCompatActivity {
    private ImageView mPoster;

    private String[] mMovieInfo;
    private String mPosterUrlString;

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
                Picasso.with(context).load(mPosterUrlString).into(mPoster);

                DetailHelper detailHelper = new DetailHelper(context, mMovieInfo[1], mMovieInfo[3], mMovieInfo[4], mMovieInfo[5]);
                binding.setDetail(detailHelper);
            }
        }
    }
}