package com.example.madminiproject;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Movie implements Serializable {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("overview")
    private String overview; // optional

    private boolean isFavorite = false;
    private boolean isWatchlisted = false;

    public Movie(String title, String posterPath, String backdropPath, String overview) {
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
    }
    public Movie(String title, String posterPath) {
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    // ✅ Safe fallback for null overview
    public String getOverview() {
        return overview != null ? overview : "No overview available.";
    }

    public String getFullPosterUrl() {
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    public String getFullBackdropUrl() {
        return "https://image.tmdb.org/t/p/w1280" + backdropPath;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isWatchlisted() {
        return isWatchlisted;
    }

    public void setWatchlisted(boolean watchlisted) {
        isWatchlisted = watchlisted;
    }
}