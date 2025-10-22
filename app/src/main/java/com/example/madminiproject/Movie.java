package com.example.madminiproject;

import com.google.gson.annotations.SerializedName;

public class Movie {
        @SerializedName("id")
        private int id;
        @SerializedName("title")
        private String title;

        @SerializedName("poster_path")
        private String posterPath;

        public Movie(String title, String posterPath) {
                this.title = title;
                this.posterPath = posterPath;

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

        public String getFullPosterUrl() {
                // TMDB base URL for images
                return "https://image.tmdb.org/t/p/w500" + posterPath;
        }

}
