package com.example.madminiproject;

public class Profile {
    private String profileId;
    private String name;
    private String avatarUrl;
    private int colorIndex;

    // Default constructor for Firestore
    public Profile() {}

    // Constructor without colorIndex
    public Profile(String profileId, String name, String avatarUrl) {
        this.profileId = profileId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = -1;
    }
    public Profile(String name, String avatarUrl, int colorIndex) {
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = colorIndex;
    }

    // Constructor with colorIndex
    public Profile(String profileId, String name, String avatarUrl, int colorIndex) {
        this.profileId = profileId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.colorIndex = colorIndex;
    }

    // Getters
    public String getProfileId() { return profileId; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public int getColorIndex() { return colorIndex; }

    // Setters
    public void setProfileId(String profileId) { this.profileId = profileId; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setColorIndex(int colorIndex) { this.colorIndex = colorIndex; }
}
