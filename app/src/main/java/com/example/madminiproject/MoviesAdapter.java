package com.example.madminiproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {
    private final List<Movie> movies;
    private final Context context;
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    public MoviesAdapter(Context context, List<Movie> movies) {
        this.context = context;
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie m = movies.get(position);
        holder.txtTitle.setText(m.getTitle() != null ? m.getTitle() : "");

        String posterPath = m.getPosterPath();
        if (posterPath != null && !posterPath.isEmpty()) {
            String fullUrl = posterPath.startsWith("http") ? posterPath : IMAGE_BASE + posterPath;
            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(android.R.drawable.progress_indeterminate_horizontal) // builtin fallback
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgPoster);
        } else {
            // optional: show a placeholder if no poster_path
            holder.imgPoster.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("movie", m);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView txtTitle;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            txtTitle = itemView.findViewById(R.id.txtTitle);
        }
    }
}
