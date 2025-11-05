package com.example.madminiproject;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadService;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@UnstableApi
public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {

    private List<Download> downloads = new ArrayList<>();
    private Context context;

    public DownloadsAdapter(Context context) {
        this.context = context;
    }

    public void setDownloads(List<Download> downloads) {
        this.downloads = downloads;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Download download = downloads.get(position);

        // Parse Movie from download data
        Movie movie = null;
        if (download.request.data != null) {
            movie = Movie.fromJson(new String(download.request.data, StandardCharsets.UTF_8));
            if (download.request.data != null) {
                String rawData = new String(download.request.data, StandardCharsets.UTF_8);
                Log.d("DownloadsAdapter", "Raw download data: " + rawData);
            } else {
                Log.d("DownloadsAdapter", "Download data is null for id: " + download.request.id);
            }

        }

        if (movie != null) {
            holder.title.setText(movie.getTitle());
            Log.d("DownloadsAdapter", "Movie title: " + movie.getTitle());


            // Use poster for thumbnail
            Glide.with(context)
                    .load(movie.getFullPosterUrl())
                    .placeholder(R.drawable.account_icon)
                    .into(holder.thumbnail);
        } else {
            holder.title.setText(download.request.id);
        }

        // Open DetailsActivity with full Movie object
        Movie finalMovie = movie;
        holder.itemView.setOnClickListener(v -> {
            if (finalMovie != null) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("movie", finalMovie);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Movie data unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            DownloadService.sendRemoveDownload(context, DemoDownloadService.class, download.request.id, true);
            Toast.makeText(context, "Deleting " + download.request.id, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return downloads.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView thumbnail;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movie_title);
            thumbnail = itemView.findViewById(R.id.thumb_image);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
