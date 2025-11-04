package com.example.madminiproject;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    @DocumentId
    private String id;
    private String name;
    private String avatarUrl;
    private int colorIndex;
    private Object favorites; // Use Object for flexible deserialization
    private Object watchList; // Use Object for flexible deserialization

    // Default constructor for Firestore
    public Profile() {
        this.favorites = new ArrayList<String>();
        this.watchList = new ArrayList<String>();
    }

    public Profile(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = -1;
        this.favorites = new ArrayList<String>();
        this.watchList = new ArrayList<String>();
    }

    public Profile(String name, String avatarUrl, int colorIndex) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = colorIndex;
        this.favorites = new ArrayList<String>();
        this.watchList = new ArrayList<String>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getColorIndex() { return colorIndex; }

    // Raw getters/setters for Firebase
    public Object getFavorites() { return favorites; }
    public void setFavorites(Object favorites) { this.favorites = favorites; }
    public Object getWatchList() { return watchList; }
    public void setWatchList(Object watchList) { this.watchList = watchList; }

    @Exclude
    @SuppressWarnings("unchecked")
    public List<String> getFavoritesAsList() {
        if (favorites instanceof List) {
            List<?> rawList = (List<?>) favorites;
            // Check if the list is not empty and its first element is a String.
            // This helps confirm it's the new format.
            if (!rawList.isEmpty() && rawList.get(0) instanceof String) {
                return (List<String>) rawList;
            }
        }
        // If it's the old format or invalid, return an empty list.
        return new ArrayList<>();
    }

    @Exclude
    @SuppressWarnings("unchecked")
    public List<String> getWatchListAsList() {
        if (watchList instanceof List) {
            List<?> rawList = (List<?>) watchList;
            if (!rawList.isEmpty() && rawList.get(0) instanceof String) {
                return (List<String>) rawList;
            }
        }
        return new ArrayList<>();
    }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setColorIndex(int colorIndex) { this.colorIndex = colorIndex; }
}
