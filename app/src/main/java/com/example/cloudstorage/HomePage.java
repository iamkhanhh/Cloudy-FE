package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.Album;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.FolderItem;
import com.example.cloudstorage.models.Media;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomePage extends AppCompatActivity {
    private static final String TAG = "HomePage";

    private TokenManager tokenManager;
    private FlexboxLayout foldersListLayout;
    private List<FolderItem> folderItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        tokenManager = new TokenManager(this);

        DrawerLayout drawerLayout =  findViewById(R.id.main);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        TextView btn_nav_profile = findViewById(R.id.nav_profile_text);
        TextView nav_logout_text = findViewById(R.id.nav_logout_text);
        TextView nav_settings_text = findViewById(R.id.nav_settings_text);

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        ImageView closeMenuIcon = findViewById(R.id.close_menu_icon);

        closeMenuIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });

        btn_nav_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(HomePage.this, my_profiile.class);
                startActivity(it);
            }
        });

        nav_logout_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLogout();
            }
        });

        nav_settings_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(HomePage.this, change_password.class);
                startActivity(it);
            }
        });

        // Initialize folders list
        foldersListLayout = findViewById(R.id.folderslist);
        folderItems = new ArrayList<>();

        // Load albums and media from backend
        loadAlbumsAndMedia();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Load albums and media from backend
     */
    private void loadAlbumsAndMedia() {
        // Clear existing static items from layout
        foldersListLayout.removeAllViews();
        folderItems.clear();

        // Load both albums and media in parallel
        loadAlbums();
        loadMedia();
    }

    /**
     * Load albums from backend
     */
    private void loadAlbums() {
        ApiClient.getApiService(this).getAllAlbums().enqueue(new Callback<ApiResponse<List<Album>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Album>>> call, Response<ApiResponse<List<Album>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Album>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        List<Album> albums = apiResponse.getData();
                        Log.d(TAG, "Loaded " + albums.size() + " albums");

                        // Add albums to folder items
                        for (Album album : albums) {
                            folderItems.add(new FolderItem(album));
                        }

                        // Update UI
                        displayFolderItems();
                    } else {
                        Log.e(TAG, "Failed to load albums: " + apiResponse.getMessageOrDefault("Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Failed to load albums: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Album>>> call, Throwable t) {
                Log.e(TAG, "Error loading albums", t);
                Toast.makeText(HomePage.this, "Error loading albums: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load media from backend
     */
    private void loadMedia() {
        ApiClient.getApiService(this).getAllMedia().enqueue(new Callback<ApiResponse<List<Media>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Media>>> call, Response<ApiResponse<List<Media>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Media>> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        List<Media> mediaList = apiResponse.getData();
                        Log.d(TAG, "Loaded " + mediaList.size() + " media items");

                        // Add media to folder items
                        for (Media media : mediaList) {
                            folderItems.add(new FolderItem(media));
                        }

                        // Update UI
                        displayFolderItems();
                    } else {
                        Log.e(TAG, "Failed to load media: " + apiResponse.getMessageOrDefault("Unknown error"));
                    }
                } else {
                    Log.e(TAG, "Failed to load media: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Media>>> call, Throwable t) {
                Log.e(TAG, "Error loading media", t);
                Toast.makeText(HomePage.this, "Error loading media: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);;

        for (FolderItem item : folderItems) {
            View itemView = inflater.inflate(R.layout.item_folder, foldersListLayout, false);

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
                Media media = item.getMedia();
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
            }

            // Set click listener for the item
            itemView.setOnClickListener(v -> {
                if (item.isMedia()) {
                    // Navigate to MediaDetail activity
                    Intent intent = new Intent(HomePage.this, MediaDetail.class);
                    intent.putExtra("media_id", item.getId());
                    startActivity(intent);
                } else if (item.isAlbum()) {
                    Intent intent = new Intent(HomePage.this, FolderDetails.class);
                    intent.putExtra("album_id", item.getId());
                    startActivity(intent);
                }
            });

            // Set click listener for options menu
            ivFolderOptions.setOnClickListener(v -> {
                showOptionsMenu(v, item);
            });

            // Add view to layout
            foldersListLayout.addView(itemView);
        }
    }

    /**
     * Show options menu for folder item
     */
    private void showOptionsMenu(View view, FolderItem item) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.folder_option_menu, popup.getMenu());

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

    /**
     * Xử lý logout
     */
    private void handleLogout() {
        tokenManager.clearToken();
        navigateToLogin();
    }

    /**
     * Chuyển về màn hình login
     */
    private void navigateToLogin() {
        Intent intent = new Intent(HomePage.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}