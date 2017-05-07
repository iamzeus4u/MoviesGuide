package xyz.jc.zeus.moviesguide;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by zeus on 04/05/2017.
 */

public class MovieVideoAdapter extends ArrayAdapter<MovieVideo> implements YouTubeThumbnailView.OnInitializedListener {
    private final String TAG = xyz.jc.zeus.moviesguide.MovieVideoAdapter.class.getSimpleName();
    private final String THUMBNAIL_BASE_URL = "https://img.youtube.com/vi/";
    private final String THUMBNAIL_END_URL = "/hqdefault.jpg";

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {

    }

    @Override
    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

    }

    private static class ViewHolder {
        private TextView nameTextView;
        private TextView typeTextView;
        private TextView siteTextView;
        private ImageView imageView;
    }

    public MovieVideoAdapter(Context context, List<MovieVideo> videos) {
        super(context, R.layout.video_list_item, videos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final MovieVideo videos = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.video_list_item, parent, false);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textView);
            viewHolder.typeTextView = (TextView) convertView.findViewById(R.id.type_textView);
            viewHolder.siteTextView = (TextView) convertView.findViewById(R.id.site_textView);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.video_imageview);
            Picasso.with(getContext()).load(THUMBNAIL_BASE_URL + videos.getKEY + THUMBNAIL_END_URL).placeholder(R.drawable.loading).into(viewHolder.imageView);
            Log.d(TAG, THUMBNAIL_BASE_URL + videos.getKEY + THUMBNAIL_END_URL);


            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.nameTextView.setText(videos.getNAME);
        viewHolder.typeTextView.setText(videos.getTYPE);
        viewHolder.siteTextView.setText(getContext().getString(R.string.watch_video) + videos.getSITE);

        return convertView;
    }
}
