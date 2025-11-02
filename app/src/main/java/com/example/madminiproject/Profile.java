package com.example.madminiproject;

public class Profile {
    private String name;
    private String avatarUrl;

    // Default constructor for Firestore
    public Profile() {}

    public Profile(String name, String avatarUrl) {
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
