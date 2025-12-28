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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.FolderItem;
import com.example.cloudstorage.models.Share;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SharedWithMe extends AppCompatActivity {

    private static final String TAG = "SharedWithMe";

    private FlexboxLayout foldersListLayout;
    private List<FolderItem> folderItems;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shared_with_me);

        tokenManager = new TokenManager(this);

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            // Finish the activity to go back to the previous screen
            finish();
        });

        // Initialize folders list
        foldersListLayout = findViewById(R.id.folderslist);
        folderItems = new ArrayList<>();

        // Load shared items from backend
        loadSharedItems();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Load shared items from backend
     */
    private void loadSharedItems() {
        // Clear existing items
        foldersListLayout.removeAllViews();
        folderItems.clear();

        ApiClient.getApiService(this).getSharedItems().enqueue(new Callback<ApiResponse<List<Share>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Share>>> call, Response<ApiResponse<List<Share>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Share>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        List<Share> shares = apiResponse.getData();
                        Log.d(TAG, "Loaded " + shares.size() + " shared items");

                        // Convert shares to folder items
                        for (Share share : shares) {
                            FolderItem item = share.toFolderItem();
                            if (item != null) {
                                folderItems.add(item);
                            }
                        }

                        // Update UI
                        displayFolderItems();
                    } else {
                        Log.e(TAG, "Failed to load shared items: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(SharedWithMe.this, "Failed to load shared items", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load shared items: " + response.code());
                    Toast.makeText(SharedWithMe.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Share>>> call, Throwable t) {
                Log.e(TAG, "Error loading shared items", t);
                Toast.makeText(SharedWithMe.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display folder items (albums and media) in FlexboxLayout
     */
    private void displayFolderItems() {
        // Clear existing views
        foldersListLayout.removeAllViews();

        // Create view for each folder item
        LayoutInflater inflater = LayoutInflater.from(this);

        for (FolderItem item : folderItems) {

            View itemView = inflater.inflate(R.layout.item_folder,foldersListLayout , false);
            if (item.isAlbum()) {
                itemView = inflater.inflate(R.layout.item_folder_shared_album,foldersListLayout , false);

            }

            ImageView ivFolderIcon = itemView.findViewById(R.id.iv_folder_icon);
            ImageView ivFolderOptions = itemView.findViewById(R.id.iv_folder_options);
            TextView tvFolderName = itemView.findViewById(R.id.tv_folder_name);
            TextView tvFolderDate = itemView.findViewById(R.id.tv_folder_date);

            // Set folder name and date
            tvFolderName.setText(item.getName());
            tvFolderDate.setText(item.getDate());

            // Set icon based on type
            if (item.isAlbum()) {
                // Album - show folder icon
                ivFolderIcon.setImageResource(R.drawable.folder);
            } else if (item.isMedia()) {
                // Media - show thumbnail if image, or folder icon if video
                if (item.getMedia().isImage()) {
                    // Load image thumbnail using Glide
                    Glide.with(this)
                            .load(item.getMedia().getFilePath())
                            .placeholder(R.drawable.ic_images)
                            .error(R.drawable.ic_images)
                            .centerCrop()
                            .into(ivFolderIcon);
                } else {
                    // Video - use folder icon for now
                    ivFolderIcon.setImageResource(R.drawable.folder);
                }
            }

            // Set click listener for the item
            itemView.setOnClickListener(v -> {
                if (item.isMedia()) {
                    // Navigate to MediaDetail activity
                    Intent intent = new Intent(SharedWithMe.this, MediaDetail.class);
                    intent.putExtra("media_id", item.getId());
                    startActivity(intent);
                } else if (item.isAlbum()) {
                    // Navigate to Album detail activity
                    Toast.makeText(SharedWithMe.this, "Album: " + item.getName(), Toast.LENGTH_SHORT).show();
                }
            });




            // Set click listener for options menu
            if (ivFolderOptions != null) {
                ivFolderOptions.setOnClickListener(v -> {
                    showOptionsMenu(v, item);
                });
            }

            // Add view to layout
            foldersListLayout.addView(itemView);
        }
    }

    /**
     * Show options menu for folder item
     */
    private void showOptionsMenu(View view, FolderItem item) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_shared_media_options, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_edit) {
                Toast.makeText(this, "Edit: " + item.getName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_delete) {
                Toast.makeText(this, "Delete: " + item.getName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_share) {
                Toast.makeText(this, "Share: " + item.getName(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}
