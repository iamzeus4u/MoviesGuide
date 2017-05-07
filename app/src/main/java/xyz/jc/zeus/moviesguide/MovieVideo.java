package xyz.jc.zeus.moviesguide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeus on 04/05/2017.
 */

public class MovieVideo {
    final String ID = "id";
    final String NAME = "name";
    final String SITE = "site";
    final String KEY = "key";
    final String TYPE = "type";
    final String SITE_BASE_URL = "https://youtu.be/";
    public String getID;
    public String getKEY;
    public String getNAME;
    public String getSITE;
    public String getTYPE;
    public String getURL;

    public MovieVideo(JSONObject reviewInfoJson) {
        try {
            this.getID = reviewInfoJson.getString(ID);
            this.getKEY = reviewInfoJson.getString(KEY);
            this.getNAME = reviewInfoJson.getString(NAME);
            this.getSITE = reviewInfoJson.getString(SITE);
            this.getTYPE = reviewInfoJson.getString(TYPE);
            this.getURL = SITE_BASE_URL + getKEY;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<MovieVideo> fromJson(JSONArray videoData) {
        List<MovieVideo> videos = new ArrayList<MovieVideo>();
        for (int i = 0; i < videoData.length(); i++) {
            try {
                videos.add(new MovieVideo(videoData.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return videos;
    }

}
