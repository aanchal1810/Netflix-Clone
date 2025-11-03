package com.example.madminiproject.viewmodel;

import com.example.madminiproject.Movie;

public class MovieRequest {
    public String movietitle;
    public MovieRequest(String movietitle){
        this.movietitle = movietitle;
    }
    public String getMovietitle(){
        return movietitle;
    }
    public void setMovietitle(){
        this.movietitle = movietitle;
    }
}
