package xyz.jc.zeus.moviesguide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeus on 04/05/2017.
 */

public class MovieReview {
    final String ID = "id";
    final String AUTHOR = "author";
    final String CONTENT = "content";
    final String PATH = "url";
    public String getID;
    public String getAUTHOR;
    public String getCONTENT;
    public String getPATH;

    public MovieReview(JSONObject reviewInfoJson) {
        try {
            this.getID = reviewInfoJson.getString(ID);
            this.getAUTHOR = reviewInfoJson.getString(AUTHOR);
            this.getCONTENT = reviewInfoJson.getString(CONTENT);
            this.getPATH = reviewInfoJson.getString(PATH);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<MovieReview> fromJson(JSONArray reviewData) {
        List<MovieReview> reviews = new ArrayList<MovieReview>();
        for (int i = 0; i < reviewData.length(); i++) {
            try {
                reviews.add(new MovieReview(reviewData.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return reviews;
    }

}
