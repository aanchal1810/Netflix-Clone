package com.example.madminiproject.viewmodel;

import com.example.madminiproject.Movie;

public class MovieRequest {
    public String movietitle;
    public String profileID;
    
    // Default constructor for Gson
    public MovieRequest() {
    }
    
    public MovieRequest(String movietitle, String profileID){
        this.movietitle = movietitle;
        this.profileID = profileID;
    }
    
    // Constructor with only movietitle (for backward compatibility)
    public MovieRequest(String movietitle){
        this.movietitle = movietitle;
        this.profileID = "";
    }
    
    public String getMovietitle(){
        return movietitle;
    }
    
    public void setMovietitle(String movietitle){
        this.movietitle = movietitle;
    }
    
    public String getProfileID(){
        return profileID;
    }
    
    public void setProfileID(String profileID){
        this.profileID = profileID;
    }
}
