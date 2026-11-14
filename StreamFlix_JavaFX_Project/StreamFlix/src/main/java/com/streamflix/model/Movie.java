package com.streamflix.model;

/**
 * Movie - one video in the catalogue.
 *
 * PORTION: Data & Categories  |  OWNER: Rorisang Konyana
 *
 * This is an immutable "model" class (it only holds data, no UI code).
 * Every other part of the app - cards, hero banner, player - receives a Movie,
 * so keep the method names stable.
 */
public final class Movie {

    private final String title;
    private final String genre;
    private final int year;
    private final String trailerId;   // YouTube video id, e.g. "YoHD9XEInc0"
    private final String backupId;    // second YouTube id used if the first cannot be embedded

    public Movie(String title, String genre, int year, String trailerId, String backupId) {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.trailerId = trailerId;
        this.backupId = backupId;
    }

    public String getTitle()     { return title; }
    public String getGenre()     { return genre; }
    public int getYear()         { return year; }
    public String getTrailerId() { return trailerId; }
    public String getBackupId()  { return backupId; }

    public boolean hasBackup() {
        return backupId != null && !backupId.isBlank() && !backupId.equals(trailerId);
    }

    /** Thumbnail image hosted by YouTube (nothing is stored in our project). */
    public String getThumbnailUrl() {
        return "https://img.youtube.com/vi/" + trailerId + "/hqdefault.jpg";
    }

    /** Large 16:9 image, used by the hero banner. Not every video has one, so callers must handle errors. */
    public String getBackdropUrl() {
        return "https://img.youtube.com/vi/" + trailerId + "/maxresdefault.jpg";
    }

    /** Normal YouTube page - used by the "Open on YouTube" button. */
    public String getWatchUrl(boolean useBackup) {
        return "https://www.youtube.com/watch?v=" + (useBackup && hasBackup() ? backupId : trailerId);
    }

    @Override
    public String toString() {
        return title + " (" + year + ")";
    }
}
