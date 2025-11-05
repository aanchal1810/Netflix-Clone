package com.example.madminiproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CardStackAdapter extends RecyclerView.Adapter<CardStackAdapter.ViewHolder> {

    private List<Movie> movies = new ArrayList<>();

    public CardStackAdapter(List<Movie> spots) {
        this.movies = spots;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.name.setText(movie.getTitle());

        //change it to .load(move.getFullPosterUrl()) i used getPosterPath for trial purpose
        Glide.with(holder.image)
                .load(movie.getPosterPath())
                .into(holder.image);

        holder.itemView.setOnClickListener(v ->
                Toast.makeText(v.getContext(), movie.getTitle(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    public void setMovies(List<Movie> spots) {
        this.movies = spots;
        notifyDataSetChanged();
    }
    
    /**
     * Adds new movies to the existing list incrementally.
     * Uses notifyItemRangeInserted for efficient updates without full refresh.
     * Filters out duplicates before adding using HashSet for O(n) performance.
     */
    public void addMovies(List<Movie> newMovies) {
        if (newMovies == null || newMovies.isEmpty()) {
            return;
        }
        
        // Use HashSet for O(n) duplicate filtering instead of O(n²) nested loops
        Set<String> existingTitles = new HashSet<>();
        for (Movie movie : movies) {
            existingTitles.add(movie.getTitle());
        }
        
        Set<String> seenTitles = new HashSet<>();
        List<Movie> uniqueNewMovies = new ArrayList<>();
        
        for (Movie newMovie : newMovies) {
            String title = newMovie.getTitle();
            // Only add if not in existing movies and not already seen in newMovies
            if (!existingTitles.contains(title) && seenTitles.add(title)) {
                uniqueNewMovies.add(newMovie);
            }
        }
        
        if (!uniqueNewMovies.isEmpty()) {
            int startPosition = movies.size();
            movies.addAll(uniqueNewMovies);
            notifyItemRangeInserted(startPosition, uniqueNewMovies.size());
        }
    }

    public List<Movie> getSpots() {
        return movies;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;

        ImageView image;

        public ViewHolder(@NonNull View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            image = view.findViewById(R.id.item_image);
        }
    }
}
