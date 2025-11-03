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

        @SerializedName("overview")
        private String overview; // optional

        private boolean isFavorite = false;

        // ✅ Constructor without overview (optional)
        public Movie(String title, String posterPath) {
                this(title, posterPath, null);
        }

        // ✅ Full constructor (if overview is provided)
        public Movie(String title, String posterPath, String overview) {
                this.title = title;
                this.posterPath = posterPath;
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

        // ✅ Safe fallback for null overview
        public String getOverview() {
                return overview != null ? overview : "No overview available.";
        }

        public String getFullPosterUrl() {
                return "https://image.tmdb.org/t/p/w500" + posterPath;
        }

        public boolean isFavorite() {
                return isFavorite;
        }

        public void setFavorite(boolean favorite) {
                isFavorite = favorite;
        }
}
