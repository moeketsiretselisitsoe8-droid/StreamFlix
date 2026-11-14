package com.streamflix.data;

import com.streamflix.model.Category;
import com.streamflix.model.Movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MovieRepository - loads the catalogue from /data/movies.csv and groups it into categories.
 *
 * PORTION: Data & Categories  |  OWNER: Rorisang Konyana
 *
 * Why a CSV file? Teammates can add or fix a movie by editing one text line,
 * without touching any Java code. (Separation of data from logic.)
 */
public final class MovieRepository {

    private static final String RESOURCE = "/data/movies.csv";

    private MovieRepository() { }

    /** Reads the CSV and returns the categories in the same order they first appear in the file. */
    public static List<Category> loadCategories() {
        Map<String, List<Movie>> byGenre = new LinkedHashMap<>();

        try (InputStream in = MovieRepository.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Cannot find " + RESOURCE + " on the classpath");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;                                  // skip comments & blank lines
                }
                String[] p = line.split("\\|");
                if (p.length < 4) {
                    System.err.println("movies.csv line " + lineNo + " ignored (needs at least 4 fields): " + line);
                    continue;
                }
                String genre = p[0].trim();
                String title = p[1].trim();
                int year = parseYear(p[2].trim());
                String id = p[3].trim();
                String backup = p.length > 4 ? p[4].trim() : "";
                byGenre.computeIfAbsent(genre, g -> new ArrayList<>())
                       .add(new Movie(title, genre, year, id, backup));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + RESOURCE, e);
        }

        List<Category> result = new ArrayList<>();
        byGenre.forEach((name, movies) -> result.add(new Category(name, movies)));
        return result;
    }

    private static int parseYear(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
