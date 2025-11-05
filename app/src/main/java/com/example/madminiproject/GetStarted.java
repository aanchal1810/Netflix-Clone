package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class GetStarted extends AppCompatActivity {

    int[] backgrounds = {
            R.drawable.login_bg1,
            R.drawable.login_bg2,
            R.drawable.login_bg3,
            R.drawable.login_bg4
    };

    String[] texts = {
            "Unlimited entertainment, one low price",
            "Download and watch offline, anytime",
            "Watch everywhere",
            "Cancel Online anytime"
    };

    String[] subtexts = {
            "All of Netflix, starting at just Rs.149.",
            "Always have something to watch.",
            "Stream on your phone, tablet, laptop, TV and more.",
            "Join today, no reason to wait."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_started);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        View topNavbar = findViewById(R.id.topNavbar);
        topNavbar.setElevation(10f);
        topNavbar.bringToFront();
        topNavbar.invalidate();

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        TextView carouselText = findViewById(R.id.carouselText);
        TextView carouselSubText = findViewById(R.id.carouselSubText);
        ConstraintLayout mainLayout = findViewById(R.id.main);
        Button btnGetStarted = findViewById(R.id.button);

        // 1) set adapter first
        viewPager.setAdapter(new SimplePagerAdapter(texts.length));

        // 2) then attach TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {}).attach();
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
            p.setMargins(16, 0, 16, 0); // adds horizontal spacing
            tab.requestLayout();
        }

        // 3) update background & text on page change
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int idx = position % backgrounds.length;
                mainLayout.setBackgroundResource(backgrounds[idx]);
                carouselText.setText(texts[idx]);
                carouselSubText.setText(subtexts[idx]);

            }
        });

        btnGetStarted.setOnClickListener(v -> {
            startActivity(new Intent(GetStarted.this, LoginActivity.class));
            finish();
        });
    }
}
