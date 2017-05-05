package xyz.jc.zeus.moviesguide.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

/**
 * Created by zeus on 03/05/2017.
 */


public interface MovieColumns {

    /* String array to hold each movie information String
        * id = 0
        * original_title = 1
        * poster_path = 2
        * overview = 3
        * release_date = 4
        * vote_average = 5
        * */
    @DataType(INTEGER)
    @PrimaryKey
    @AutoIncrement
    String _ID = "_id";
    @DataType(TEXT)
    @NotNull
    String ID = "id";
    @DataType(TEXT)
    @NotNull
    String ORIGINAL_TITLE = "original_title";
    @DataType(TEXT)
    @NotNull
    String POSTER_PATH = "poster_path";
    @DataType(TEXT)
    @NotNull
    String MOVIES_OVERVIEW = "overview";
    @DataType(TEXT)
    @NotNull
    String RELEASE_DATE = "release_date";
    @DataType(TEXT)
    @NotNull
    String USER_RATING = "vote_average";
    @DataType(TEXT)
    @NotNull
    String JSON_REVIEW = "jSONreview";
}
