package com.example.madminiproject;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<Profile> profiles;
    private final OnProfileSelectedListener listener;

    public interface OnProfileSelectedListener {
        void onProfileSelected(Profile profile);
    }

    public ProfileAdapter(List<Profile> profiles, OnProfileSelectedListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profiles.get(position);
        holder.bind(profile, listener);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarImageView;
        private final TextView nameTextView;
        private final Animation scaleUp = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_up);
        private final Animation scaleDown = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_down);

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.profile_avatar);
            nameTextView = itemView.findViewById(R.id.profile_name);
        }

        public void bind(final Profile profile, final OnProfileSelectedListener listener) {
            nameTextView.setText(profile.getName());

            Glide.with(itemView.getContext())
                    .load(profile.getAvatarUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(avatarImageView);

            itemView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.startAnimation(scaleUp);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    v.startAnimation(scaleDown);
                    v.postDelayed(() -> listener.onProfileSelected(profile), 100);
                }
                return true;
            });
        }
    }
}
