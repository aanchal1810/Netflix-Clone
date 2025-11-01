package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.madminiproject.Movie;
import java.util.ArrayList;
import java.util.List;

public class OnboardingViewModel extends ViewModel {

    private final MutableLiveData<List<Movie>> movies = new MutableLiveData<>();

    public OnboardingViewModel() {
        movies.setValue(createSpots());
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }

    private List<Movie> createSpots() {
        List<Movie> spots = new ArrayList<>();
        spots.add(new Movie("Moana", "https://i.pinimg.com/1200x/4a/d6/c0/4ad6c0738cb7b23bc9dac91e1d37d770.jpg"));
        spots.add(new Movie("Frozen", "https://i.pinimg.com/736x/c5/7a/a1/c57aa1543487d2bcb69c0217bead64a8.jpg"));
        spots.add(new Movie("Tangled", "https://i.pinimg.com/736x/3c/69/31/3c69316b0386a0548947b68408446472.jpg"));
        spots.add(new Movie("Confessions of a Shopaholic", "https://i.pinimg.com/736x/79/d1/15/79d115e00256c8b89446f39a9a618dd7.jpg"));
        spots.add(new Movie("The devil wears the Prada", "https://i.pinimg.com/1200x/50/6a/e1/506ae1e81ceaafa792c8642fde804298.jpg"));
        spots.add(new Movie("Mama Mia!", "https://i.pinimg.com/1200x/ea/a7/13/eaa7133c088208a12df356c0f9d4120f.jpg"));
        spots.add(new Movie("Monte Carlo", "https://i.pinimg.com/1200x/f4/ab/67/f4ab6785a255b37e8431d3aa12875f82.jpg"));
        spots.add(new Movie("27 Dresses", "https://i.pinimg.com/736x/71/65/57/7165578b8b040424fe6e847ed5c8cca3.jpg"));
        spots.add(new Movie("Bride Wars", "https://i.pinimg.com/736x/9b/f7/44/9bf744c77dcf1192c721ba229e0d3c26.jpg"));
        spots.add(new Movie("13 Going on 30", "https://i.pinimg.com/736x/fa/26/46/fa2646d5fccc0d1a4f95efc667fc2b14.jpg"));
        return spots;
    }
}
