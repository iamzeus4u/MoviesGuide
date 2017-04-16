package xyz.jc.zeus.moviesguide;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DetailActivity extends AppCompatActivity {

    private TextView mTitle;
    private TextView mRating;
    private TextView mDate;
    private TextView mOverview;
    private ImageView mPoster;

    private String[] mMovieInfo ;
    private String mPosterUrlString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mTitle = (TextView) findViewById(R.id.movie_detail_title);
        mRating = (TextView) findViewById(R.id.movie_detail_rating);
        mDate = (TextView) findViewById(R.id.movie_detail_date);
        mOverview = (TextView) findViewById(R.id.movie_detail_overview);
        mPoster = (ImageView) findViewById(R.id.movie_detail_poster);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mMovieInfo = intentThatStartedThisActivity.getStringArrayExtra("movie");

                mPosterUrlString = mMovieInfo[2].replace("w342","w780");
                try {
                    Date a = new SimpleDateFormat("yyyy-MM-dd").parse(mMovieInfo[4]);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Context context = this;

                mTitle.setText(mMovieInfo[1]);
                mOverview.setText(mMovieInfo[3]);
                mDate.setText(mMovieInfo[4]);
                mRating.append(mMovieInfo[5]);
                Picasso.with(context).load(mPosterUrlString).into(mPoster);
            }
        }
    }
}