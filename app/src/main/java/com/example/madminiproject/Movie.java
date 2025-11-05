package com.example.madminiproject;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;
import java.io.Serializable;

public class Movie implements Serializable {
    private int id;
    private String title;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("backdrop_path")

    private String backdropPath;
    private String overview;
    private boolean isFavorite = false;
    private boolean isWatchlisted = false;

    // 🔹 New field for local/offline URI
    private String localUri;

    public Movie() {}

    public Movie(String title, String posterPath, String backdropPath, String overview) {
        this.title = title;
        this.posterPath = (posterPath != null && !posterPath.isEmpty()) ? posterPath : null;
        this.backdropPath = (backdropPath != null && !backdropPath.isEmpty()) ? backdropPath : null;
        this.overview = overview != null ? overview : "No overview available.";
    }

    public static Movie fromJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            Movie movie = new Movie();
            movie.setTitle(obj.optString("title"));
            movie.setOverview(obj.optString("overview"));

            // Use thumbnail if poster_path/backdrop_path not available
            String poster = obj.optString("poster_path", obj.optString("thumbnail", null));
            movie.setPosterPath(poster);

            String backdrop = obj.optString("backdrop_path", obj.optString("thumbnail", null));
            movie.setBackdropPath(backdrop);

            return movie;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public Movie(String title, String posterPath) {
        this.title = title;
        this.posterPath = posterPath;
    }


    // ✅ Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview != null ? overview : "No overview available."; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getFullBackdropUrl() { return backdropPath != null ? "https://image.tmdb.org/t/p/w1280" + backdropPath : null; }
    public boolean isFavorite() { return isFavorite; }
    public boolean isWatchlisted() { return isWatchlisted; }
    public String getLocalUri() { return localUri; }
    public String getFullPosterUrl() {
        return posterPath != null ? "https://image.tmdb.org/t/p/w500" + posterPath : null;
    }




    // ✅ Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setOverview(String overview) { this.overview = overview; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setWatchlisted(boolean watchlisted) { isWatchlisted = watchlisted; }
    public void setLocalUri(String localUri) { this.localUri = localUri; }
}
