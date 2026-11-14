package com.streamflix.model;

import java.util.Collections;
import java.util.List;

/**
 * Category - a movie genre and the movies that belong to it (e.g. "Action").
 *
 * PORTION: Data & Categories  |  OWNER: Rorisang Konyana
 */
public final class Category {

    private final String name;
    private final List<Movie> movies;

    public Category(String name, List<Movie> movies) {
        this.name = name;
        this.movies = Collections.unmodifiableList(movies);
    }

    public String getName()       { return name; }
    public List<Movie> getMovies() { return movies; }
    public int size()             { return movies.size(); }
}
