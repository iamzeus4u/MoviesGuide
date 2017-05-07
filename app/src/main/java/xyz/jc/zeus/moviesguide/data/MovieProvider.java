package xyz.jc.zeus.moviesguide.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = MovieProvider.AUTHORITY, database = MovieDatabase.class)
public final class MovieProvider {

    public static final String AUTHORITY = "xyz.jc.zeus.moviesguide.data.MovieProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }
    @TableEndpoint(table = MovieDatabase.FAV_MOVIES)
    public static class Movies {
        private static final String PATH = "movies";

        @ContentUri(
                path = PATH,
                type = "vnd.android.cursor.dir/movie",
                defaultSort = MovieColumns.ORIGINAL_TITLE + " ASC")
        public static final Uri CONTENT_URI = buildUri(PATH);

        @InexactContentUri(
                name = MovieColumns.ID,
                path = PATH + "/#",
                type = "vnd.android.cursor.item/movie",
                whereColumn = MovieColumns.ID,
                pathSegment = 1)
        public static Uri withId(String id) {
            return buildUri(PATH, id);
        }
    }
}

