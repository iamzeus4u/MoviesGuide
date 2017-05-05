package xyz.jc.zeus.moviesguide.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

@Database(version = MovieDatabase.VERSION)
public final class MovieDatabase {
    private MovieDatabase() {
    }

    public static final int VERSION = 1;

    @Table(MovieColumns.class)
    public static final String FAV_MOVIES = "movies";
/*
    @Table(ReviewColumns.class)
    public static final String FAV_MOVIES_REVIEWS = "reviews";*/
}
