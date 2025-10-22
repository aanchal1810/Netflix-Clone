package com.example.madminiproject;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Simple empty pages adapter — pages are just empty frames so ViewPager2 can swipe.
 */
public class SimplePagerAdapter extends RecyclerView.Adapter<SimplePagerAdapter.PagerViewHolder> {

    private final int pageCount;

    public SimplePagerAdapter(int pageCount) {
        this.pageCount = pageCount;
    }

    @NonNull
    @Override
    public PagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout frame = new FrameLayout(parent.getContext());
        frame.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return new PagerViewHolder(frame);
    }

    @Override
    public void onBindViewHolder(@NonNull PagerViewHolder holder, int position) {
        // nothing to bind — background and text are handled from LoginActivity
    }

    @Override
    public int getItemCount() {
        return pageCount;
    }

    static class PagerViewHolder extends RecyclerView.ViewHolder {
        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
