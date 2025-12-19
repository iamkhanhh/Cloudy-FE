package com.example.cloudstorage;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.Media;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaDetail extends AppCompatActivity {

    private static final String TAG = "MediaDetail";

    // UI Components
    private ImageView btnBack;
    private TextView tvMediaName;
    private ImageView ivMedia;
    private VideoView vvMedia;
    private ProgressBar progressBar;
    private TextView tvCaption;
    private TextView tvFileSize;
    private TextView tvCreatedAt;

    // Data
    private int mediaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_media_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initViews();

        // Get media ID from intent
        mediaId = getIntent().getIntExtra("media_id", -1);

        if (mediaId == -1) {
            Toast.makeText(this, "Invalid media ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Load media details from backend
        loadMediaDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvMediaName = findViewById(R.id.tv_media_name);
        ivMedia = findViewById(R.id.iv_media);
        vvMedia = findViewById(R.id.vv_media);
        progressBar = findViewById(R.id.progress_bar);
        tvCaption = findViewById(R.id.tv_caption);
        tvFileSize = findViewById(R.id.tv_file_size);
        tvCreatedAt = findViewById(R.id.tv_created_at);
    }

    private void loadMediaDetails() {
        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        ivMedia.setVisibility(View.GONE);
        vvMedia.setVisibility(View.GONE);

        // Call API
        ApiClient.getApiService(this).getMediaById(mediaId).enqueue(new Callback<ApiResponse<Media>>() {
            @Override
            public void onResponse(Call<ApiResponse<Media>> call, Response<ApiResponse<Media>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Media> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        Media media = apiResponse.getData();
                        displayMedia(media);
                    } else {
                        showError("Failed to load media: " + apiResponse.getMessageOrDefault("Unknown error"));
                    }
                } else {
                    showError("Failed to load media: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Media>> call, Throwable t) {
                Log.e(TAG, "Error loading media", t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayMedia(Media media) {
        // Hide loading
        progressBar.setVisibility(View.GONE);

        // Set media name
        tvMediaName.setText(media.getFilename());

        // Set caption
        String caption = media.getCaption() != null && !media.getCaption().isEmpty()
                ? media.getCaption()
                : "No caption";
        tvCaption.setText("Caption: " + caption);

        // Set file size
        tvFileSize.setText("File Size: " + media.getFormattedSize());

        // Set created date
        tvCreatedAt.setText("Created At: " + media.getFormattedCreatedAt());

        // Load media file (image or video)
        if (media.isImage()) {
            displayImage(media.getFilePath());
        } else if (media.isVideo()) {
            displayVideo(media.getFilePath());
        }
    }

    private void displayImage(String imageUrl) {
        ivMedia.setVisibility(View.VISIBLE);
        vvMedia.setVisibility(View.GONE);

        // Load image using Glide
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_images)
                .error(R.drawable.ic_images)
                .into(ivMedia);
    }

    private void displayVideo(String videoUrl) {
        ivMedia.setVisibility(View.GONE);
        vvMedia.setVisibility(View.VISIBLE);

        // Setup video view
        vvMedia.setVideoURI(Uri.parse(videoUrl));

        // Add media controller for play/pause/seek
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(vvMedia);
        vvMedia.setMediaController(mediaController);

        // Start playing
        vvMedia.setOnPreparedListener(mp -> {
            vvMedia.start();
        });

        vvMedia.setOnErrorListener((mp, what, extra) -> {
            Log.e(TAG, "Video error: what=" + what + ", extra=" + extra);
            Toast.makeText(MediaDetail.this, "Error playing video", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, message);
    }
}