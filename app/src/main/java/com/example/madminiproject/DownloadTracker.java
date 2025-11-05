package com.example.madminiproject;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.fragment.app.FragmentManager;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DataSource;
import androidx.media3.exoplayer.RenderersFactory;
import androidx.media3.exoplayer.offline.Download;
import androidx.media3.exoplayer.offline.DownloadCursor;
import androidx.media3.exoplayer.offline.DownloadHelper;
import androidx.media3.exoplayer.offline.DownloadIndex;
import androidx.media3.exoplayer.offline.DownloadManager;
import androidx.media3.exoplayer.offline.DownloadRequest;
import androidx.media3.exoplayer.offline.DownloadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/** Tracks downloaded media. */
@OptIn(markerClass = UnstableApi.class)
public class DownloadTracker {

    public interface Listener {
        void onDownloadsChanged();
    }

    private static final String TAG = "DownloadTracker";

    private final Context context;
    private final DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();
    private final HashMap<Uri, Download> downloads = new HashMap<>();
    private final DownloadIndex downloadIndex;

    @Nullable private StartDownloadDialogHelper startDownloadDialogHelper;

    public DownloadTracker(Context context, DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        this.downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(MediaItem mediaItem) {
        Download download = downloads.get(mediaItem.localConfiguration.uri);
        return download != null && download.state != Download.STATE_FAILED;
    }

    public Collection<Download> getDownloads() {
        return downloads.values();
    }

    @Nullable
    public DownloadRequest getDownloadRequest(Uri uri) {
        Download download = downloads.get(uri);
        return download != null && download.state != Download.STATE_FAILED ? download.request : null;
    }

    public void toggleDownload(FragmentManager fragmentManager, MediaItem mediaItem, RenderersFactory renderersFactory) {
        Download download = downloads.get(mediaItem.localConfiguration.uri);
        if (download != null && download.state != Download.STATE_FAILED) {
            DownloadService.sendRemoveDownload(context, DemoDownloadService.class, download.request.id, false);
        } else {
            if (startDownloadDialogHelper != null) {
                startDownloadDialogHelper.release();
            }
            startDownloadDialogHelper = new StartDownloadDialogHelper(
                    fragmentManager,
                    DownloadHelper.forMediaItem(context, mediaItem, renderersFactory, dataSourceFactory),
                    mediaItem
            );
        }
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            Log.w(TAG, "Failed to query downloads", e);
        }
    }

    public static class MovieMetadata {
        public final String title;
        public final String overview;
        public final String thumbnailUrl;

        private MovieMetadata(String title, String overview, String thumbnailUrl) {
            this.title = title;
            this.overview = overview;
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    @Nullable
    public static MovieMetadata getMovieMetadata(Download download) {
        if (download.request.data == null) {
            return null;
        }
        try {
            String jsonStr = new String(download.request.data, StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonStr);
            String title = json.optString("title");
            String overview = json.optString("overview");
            String thumbnailUrl = json.optString("thumbnail");
            return new MovieMetadata(title, overview, thumbnailUrl);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse download data", e);
            return null;
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {
        @Override
        public void onDownloadChanged(DownloadManager manager, Download download, @Nullable Exception e) {
            downloads.put(download.request.uri, download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager manager, Download download) {
            downloads.remove(download.request.uri);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }

    /** Helper class to show download confirmation dialog and start the download. */
    private final class StartDownloadDialogHelper implements DownloadHelper.Callback {

        private final DownloadHelper downloadHelper;
        private final MediaItem mediaItem;

        public StartDownloadDialogHelper(FragmentManager fragmentManager, DownloadHelper helper, MediaItem item) {
            this.downloadHelper = helper;
            this.mediaItem = item;
            helper.prepare(this);
        }

        public void release() {
            downloadHelper.release();
        }

        @Override
        public void onPrepared(DownloadHelper helper, boolean tracksInfoAvailable) {
            JSONObject data = new JSONObject();
            try {
                data.put("mediaId", mediaItem.mediaId);
                if (mediaItem.mediaMetadata.title != null) {
                    data.put("title", mediaItem.mediaMetadata.title);
                }
                if (mediaItem.mediaMetadata.description != null) {
                    data.put("overview", mediaItem.mediaMetadata.description);
                }
                if (mediaItem.mediaMetadata.artworkUri != null) {
                    data.put("thumbnail", mediaItem.mediaMetadata.artworkUri.toString());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON for download data", e);
            }
            startDownload(helper.getDownloadRequest(data.toString().getBytes(StandardCharsets.UTF_8)));
        }

        @Override
        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(context, "Download prepare failed", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Download prepare failed", e);
        }

        private void startDownload(DownloadRequest request) {
            DownloadService.sendAddDownload(context, DemoDownloadService.class, request, true);
        }
    }
}
