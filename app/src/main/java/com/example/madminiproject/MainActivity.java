package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madminiproject.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private MainViewModel mainViewModel;
    private final List<Movie> movieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);
        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MoviesAdapter(this, movieList);
        recyclerView.setAdapter(adapter);
        View navbar = findViewById(R.id.navbar);
        ImageView searchIcon = navbar.findViewById(R.id.search);
        searchIcon.setOnClickListener(v -> {
            startActivity(new Intent(this, Search.class));
        });
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.getMovieList().observe(this, movies -> {
            movieList.clear();
            movieList.addAll(movies);
            adapter.notifyDataSetChanged();
        });
    }
}
