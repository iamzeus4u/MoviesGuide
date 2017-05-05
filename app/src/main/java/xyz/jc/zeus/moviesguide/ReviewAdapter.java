package xyz.jc.zeus.moviesguide;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zeus on 04/05/2017.
 */

public class ReviewAdapter extends ArrayAdapter<MovieReview> {

    private static class ViewHolder {
        private TextView authorTextView;
        private TextView contentTextView;
    }

    public ReviewAdapter(Context context, List<MovieReview> reviews) {
        super(context, R.layout.review_list_item, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        MovieReview reviews = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.review_list_item, parent, false);
            viewHolder.authorTextView = (TextView) convertView.findViewById(R.id.author_textView);
            viewHolder.contentTextView = (TextView) convertView.findViewById(R.id.content_textView);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Populate the data from the data object via the viewHolder object
        // into the template view.
        viewHolder.authorTextView.setText(getContext().getString(R.string.reviewed_by) + reviews.getAUTHOR);
        viewHolder.contentTextView.setText(reviews.getCONTENT);
        // Return the completed view to render on screen
        return convertView;
    }
}
