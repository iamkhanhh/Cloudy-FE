package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.Album;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.Media;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FolderDetails extends AppCompatActivity {

    private static final String TAG = "FolderDetails";

    private int albumId = -1;
    private FlexboxLayout foldersListLayout;
    private List<Media> mediaList;
    private TextView tvAlbumName;
    private TextView tvAlbumDescription;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_folder_details);

        // Get album_id from intent
        albumId = getIntent().getIntExtra("album_id", -1);
        if (albumId == -1) {
            Toast.makeText(this, "Invalid album ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup drawer layout
        drawerLayout = findViewById(R.id.main);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Initialize views
        tvAlbumName = findViewById(R.id.tv_album_name);
        tvAlbumDescription = findViewById(R.id.tv_album_description);
        foldersListLayout = findViewById(R.id.folderslist);
        mediaList = new ArrayList<>();

        // Load album details and media
        loadAlbumDetails();
        loadAlbumMedia();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Load album details from backend
     */
    private void loadAlbumDetails() {
        ApiClient.getApiService(this).getAlbumById(albumId).enqueue(new Callback<ApiResponse<Album>>() {
            @Override
            public void onResponse(Call<ApiResponse<Album>> call, Response<ApiResponse<Album>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Album> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        Album album = apiResponse.getData();
                        Log.d(TAG, "Loaded album: " + album.getName());

                        // Display album name
                        tvAlbumName.setText(album.getName());

                        // Display album description if available
                        if (album.getDescription() != null && !album.getDescription().trim().isEmpty()) {
                            tvAlbumDescription.setText(album.getDescription());
                            tvAlbumDescription.setVisibility(View.VISIBLE);
                        } else {
                            tvAlbumDescription.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Failed to load album: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(FolderDetails.this, "Failed to load album details", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load album: " + response.code());
                    Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Album>> call, Throwable t) {
                Log.e(TAG, "Error loading album", t);
                Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load media items in this album from backend
     */
    private void loadAlbumMedia() {
        // Clear existing items
        foldersListLayout.removeAllViews();
        mediaList.clear();

        ApiClient.getApiService(this).getMediaByAlbumId(albumId).enqueue(new Callback<ApiResponse<List<Media>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Media>>> call, Response<ApiResponse<List<Media>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Media>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        mediaList = apiResponse.getData();
                        Log.d(TAG, "Loaded " + mediaList.size() + " media items");

                        // Update UI
                        displayMediaItems();
                    } else {
                        Log.e(TAG, "Failed to load media: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(FolderDetails.this, "Failed to load media", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load media: " + response.code());
                    Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Media>>> call, Throwable t) {
                Log.e(TAG, "Error loading media", t);
                Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display media items in FlexboxLayout
     */
    private void displayMediaItems() {
        // Clear existing views
        foldersListLayout.removeAllViews();

        // Create view for each media item
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Media media : mediaList) {
            View itemView = inflater.inflate(R.layout.item_folder, foldersListLayout, false);

            ImageView ivFolderIcon = itemView.findViewById(R.id.iv_folder_icon);
            ImageView ivFolderOptions = itemView.findViewById(R.id.iv_folder_options);
            TextView tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            TextView tvFolderDate = itemView.findViewById(R.id.tv_folder_date);

            // Set media name and date
            tvFolderName.setText(media.getFilename());
            tvFolderDate.setText(media.getFormattedCreatedAt());

            // Set icon based on media type
            if (media.isImage()) {
                // Load image thumbnail using Glide
                Glide.with(this)
                        .load(media.getFilePath())
                        .placeholder(R.drawable.folder)
                        .error(R.drawable.folder)
                        .centerCrop()
                        .into(ivFolderIcon);
            } else {
                // Video - use folder icon for now
                ivFolderIcon.setImageResource(R.drawable.folder);
            }

            // Set click listener for the item
            itemView.setOnClickListener(v -> {
                // Navigate to MediaDetail activity
                Intent intent = new Intent(FolderDetails.this, MediaDetail.class);
                intent.putExtra("media_id", media.getId());
                startActivity(intent);
            });

            // Set click listener for options menu
            ivFolderOptions.setOnClickListener(v -> {
                showOptionsMenu(v, media);
            });

            // Add view to layout
            foldersListLayout.addView(itemView);
        }
    }

    /**
     * Show options menu for media item
     */
    private void showOptionsMenu(View view, Media media) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.folder_option_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_edit) {
                Toast.makeText(this, "Edit: " + media.getFilename(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_delete) {
                Toast.makeText(this, "Delete: " + media.getFilename(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_share) {
                Toast.makeText(this, "Share: " + media.getFilename(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
