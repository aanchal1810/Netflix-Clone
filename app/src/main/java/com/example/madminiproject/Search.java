package com.example.madminiproject;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.madminiproject.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Search extends AppCompatActivity {
    private static final int VOICE_REQUEST_CODE = 100;
    private ImageView backButton, microphone;
    private EditText searchInput;
    private RecyclerView searchRecycler;
    private MoviesAdapter adapter;
    private List<Movie> searchList = new ArrayList<>();
    private SearchViewModel searchViewModel;
    private String profileId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        backButton = findViewById(R.id.backButton);
        searchInput = findViewById(R.id.search_item);
        searchRecycler = findViewById(R.id.recyclerViewSearch);
        microphone = findViewById(R.id.microphone);

        profileId = getIntent().getStringExtra("profileId");

        adapter = new MoviesAdapter(this, searchList,profileId);
        searchRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        searchRecycler.setAdapter(adapter);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        //for going back to the home page i.e. main activity
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(Search.this, MainActivity.class);
            startActivity(intent);
        });
        //for continuously listening to any changes in search input
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchViewModel.searchMovies(s.toString());
            }
        });
        //for observing live results
        searchViewModel.getSearchResults().observe(this, results->{
            searchList.clear();
            searchList.addAll(results);
            adapter.notifyDataSetChanged();
        });
        microphone.setOnClickListener(v -> startVoiceRecognition());

    }
    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the movie name...");
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String voiceQuery = matches.get(0);
                searchInput.setText(voiceQuery);
                searchViewModel.searchMovies(voiceQuery);
            }
        }
    }
}
