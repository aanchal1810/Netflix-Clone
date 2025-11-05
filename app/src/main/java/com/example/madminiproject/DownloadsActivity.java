package com.example.madminiproject;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadCursor;
import androidx.media3.exoplayer.offline.DownloadIndex;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public class DownloadsActivity extends AppCompatActivity implements DownloadTracker.Listener {

    private RecyclerView recyclerView;
    private DownloadsAdapter adapter;
    private DownloadTracker downloadTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_downloads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Downloads");

        recyclerView = findViewById(R.id.downloads_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DownloadsAdapter(this);
        recyclerView.setAdapter(adapter);

        downloadTracker = DemoUtil.getDownloadTracker(this);
        downloadTracker.addListener(this);

        loadDownloads();
    }

    private void loadDownloads() {
        new Thread(() -> {
            DownloadManager downloadManager = DemoUtil.getDownloadManager(this);
            DownloadIndex index = downloadManager.getDownloadIndex();

            List<Download> downloadsList = new ArrayList<>();
            try (DownloadCursor cursor = index.getDownloads(Download.STATE_COMPLETED)) {
                while (cursor.moveToNext()) {
                    downloadsList.add(cursor.getDownload());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> adapter.setDownloads(downloadsList));
        }).start();
    }

    @Override
    public void onDownloadsChanged() {
        loadDownloads();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        downloadTracker.removeListener(this);
    }
}
