package com.example.madminiproject;

public class Profile {
    private String name;
    private String avatarUrl;
    private int colorIndex;

    // Default constructor for Firestore
    public Profile() {}

    public Profile(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = -1;
    }

    public Profile(String name, String avatarUrl, int colorIndex) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = colorIndex;
    }

    public String getName() {
        return name;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public int getColorIndex() { return colorIndex; }

    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setColorIndex(int colorIndex) { this.colorIndex = colorIndex; }
}
