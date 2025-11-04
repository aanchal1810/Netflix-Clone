package com.example.madminiproject;

import android.view.LayoutInflater;
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

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_PROFILE = 0;
    private static final int VIEW_TYPE_ADD_BUTTON = 1;

    private final int[] profileBackgrounds = {
            R.drawable.profile_red,
            R.drawable.profile_blue,
            R.drawable.profile_green,
            R.drawable.profile_pink,
            R.drawable.profile_purple
    };

    private boolean profileLimitReached = false;
    private List<Profile> profiles;
    private final OnProfileSelectedListener listener;

    public interface OnProfileSelectedListener {
        void onProfileSelected(Profile profile, View sharedView);
        void onAddProfileClicked();
    }

    public ProfileAdapter(List<Profile> profiles, OnProfileSelectedListener listener) {
        this.profiles = profiles;
        this.listener = listener;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    public void setProfileLimitReached(boolean limitReached) {
        this.profileLimitReached = limitReached;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (!profileLimitReached && position == profiles.size()) {
            return VIEW_TYPE_ADD_BUTTON;
        } else {
            return VIEW_TYPE_PROFILE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PROFILE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_profile, parent, false);
            return new ProfileViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_item_profile, parent, false);
            return new AddProfileViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_PROFILE) {
            Profile profile = profiles.get(position);
            int colorResId = profileBackgrounds[
                    Math.max(0, Math.min(profile.getColorIndex(), profileBackgrounds.length - 1))
                    ];

            ProfileViewHolder viewHolder = (ProfileViewHolder) holder;
            viewHolder.bind(profile, listener, colorResId);

            // Set unique transition name for shared element animation
            String transitionName = "profile_transition_" + position;
            viewHolder.avatarImageView.setTransitionName(transitionName);
        } else {
            ((AddProfileViewHolder) holder).bind(listener);
        }
    }

    @Override
    public int getItemCount() {
        return profileLimitReached ? profiles.size() : profiles.size() + 1;
    }

    // -------------------------
    // Profile ViewHolder
    // -------------------------
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarImageView;
        private final TextView nameTextView;
        private final Animation scaleUp;
        private final Animation scaleDown;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.profile_avatar);
            nameTextView = itemView.findViewById(R.id.profile_name);

            scaleUp = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_up);
            scaleDown = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.scale_down);
        }

        public void bind(final Profile profile,
                         final OnProfileSelectedListener listener,
                         int backgroundResId) {

            nameTextView.setText(profile.getName());
            avatarImageView.setBackgroundResource(backgroundResId);

            Glide.with(itemView.getContext())
                    .load(profile.getAvatarUrl())
                    .placeholder(backgroundResId)
                    .circleCrop()
                    .into(avatarImageView);

            itemView.setOnClickListener(v -> {
                // Animate scale up then scale back down before triggering listener
                avatarImageView.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .setDuration(100)
                        .withEndAction(() -> avatarImageView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .withEndAction(() ->
                                        listener.onProfileSelected(profile, avatarImageView))
                                .start())
                        .start();
            });
        }
    }

    // -------------------------
    // Add Profile ViewHolder
    // -------------------------
    static class AddProfileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView addIcon;
        private final TextView addText;

        public AddProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            addIcon = itemView.findViewById(R.id.add_profile_icon);
            addText = itemView.findViewById(R.id.add_profile_text);
        }

        public void bind(final OnProfileSelectedListener listener) {
            itemView.setOnClickListener(v -> listener.onAddProfileClicked());
        }
    }
}
