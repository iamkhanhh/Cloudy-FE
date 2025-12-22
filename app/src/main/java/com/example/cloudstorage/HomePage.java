package com.example.cloudstorage;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.Album;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.CreateMediaRequest;
import com.example.cloudstorage.models.CreateShareRequest;
import com.example.cloudstorage.models.FolderItem;
import com.example.cloudstorage.models.Media;
import com.example.cloudstorage.models.PresignedUrl;
import com.example.cloudstorage.models.PresignedUrlRequest;
import com.example.cloudstorage.models.Share;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.flexbox.FlexboxLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.textfield.TextInputEditText;


public class HomePage extends BaseActivity {
    private static final String TAG = "HomePage";

    private TokenManager tokenManager;
    private FlexboxLayout foldersListLayout;
    private List<FolderItem> folderItems;

    private ImageView addFileButton;
    private ImageView imagePreview; // To hold a reference to the ImageView in the dialog
    private Uri selectedMediaUri; // To store the URI of the selected image

    // Handles the result from the image picker
    // Handles the result from the image picker
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedMediaUri = result.getData().getData();
                    if (imagePreview != null && selectedMediaUri != null) {
                        // Kiểm tra loại MIME của file được chọn
                        String mimeType = getContentResolver().getType(selectedMediaUri);

                        if (mimeType != null && mimeType.startsWith("video")) {
                            // Nếu là video, hiển thị frame đầu tiên làm thumbnail
                            // Glide có thể làm điều này một cách tự động
                            Glide.with(this)
                                    .load(selectedMediaUri)
                                    .placeholder(R.drawable.ic_launcher_background) // Ảnh giữ chỗ
                                    .error(R.drawable.ic_launcher_foreground) // Ảnh lỗi (thay bằng icon video)
                                    .into(imagePreview);
                        } else {
                            // Nếu là ảnh (hoặc không xác định được), hiển thị như bình thường
                            Glide.with(this)
                                    .load(selectedMediaUri)
                                    .placeholder(R.drawable.ic_launcher_background) // Ảnh giữ chỗ
                                    .into(imagePreview);
                        }
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home_page;
    }


    @Override
    protected void initViews() {
        tokenManager = new TokenManager(this);

        DrawerLayout drawerLayout =  findViewById(R.id.main);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        TextView btn_nav_profile = findViewById(R.id.nav_profile_text);
        TextView nav_logout_text = findViewById(R.id.nav_logout_text);
        TextView nav_settings_text = findViewById(R.id.nav_settings_text);
        TextView nav_share_text = findViewById(R.id.nav_shared_text);
        TextView nav_storage_text = findViewById(R.id.nav_storage_text);
        TextView nav_help_text = findViewById(R.id.nav_help_text);

        addFileButton = findViewById(R.id.add_file_button);

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

        nav_share_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(HomePage.this, SharedWithMe.class);
                startActivity(it);
            }
        });

        nav_storage_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(HomePage.this, storage_details.class);
                startActivity(it);
            }
        });

        nav_help_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(HomePage.this, help_report.class);
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

        // Set the click listener
        addFileButton.setOnClickListener(v -> showUploadDialog());

    }

    @Override
    protected void onRefreshData() {
        Log.d("HomePage", "onRefreshData called");
        loadAlbumsAndMedia();
        new Handler().postDelayed(() -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }},2000);


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
        //TODO: fallback lúc đang load ảnh

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
        // 1. Tạo PopupMenu
        PopupMenu popup = new PopupMenu(this, view);
        View itemView = (View) view.getParent().getParent(); // Điều chỉnh nếu cần
        TextView nameTextView = itemView.findViewById(R.id.tv_folder_name);
        if (nameTextView == null) {
            Log.e(TAG, "Could not find name TextView in itemView.");
            // Fallback: Tải lại toàn bộ trang nếu không tìm thấy TextView
            // (Đây là giải pháp an toàn để tránh crash và vẫn cập nhật được)
            nameTextView = new TextView(this); // Tạo một đối tượng giả để tránh lỗi
        }

        // 2. Chọn file menu dựa trên loại item
        if (item.getType() == 1) {
//        public static final int TYPE_MEDIA = 1;
            // Nếu là file, dùng menu có đủ tùy chọn
            popup.getMenuInflater().inflate(R.menu.menu_media_options, popup.getMenu());
        } else if (item.getType()== 0) {
//        public static final int TYPE_ALBUM = 0;
            // Nếu là thư mục, dùng menu đã lược bỏ
            popup.getMenuInflater().inflate(R.menu.menu_folder_options, popup.getMenu());
        } else {
            // Trường hợp khác, có thể không hiển thị menu hoặc dùng menu mặc định
            return;
        }

        final TextView finalNameTextView = nameTextView; // Cần một biến final để dùng trong lambda
        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (itemId == R.id.menu_edit) {
                if (item.isMedia()) {
                    showEditMediaDialog(item, finalNameTextView);
                } else if (item.isAlbum()) {
                    showEditAlbumDialog(item, finalNameTextView);
                }                return true;
                // xóa file
            } else if (itemId == R.id.menu_delete) {
                // Delete media or album based on item type
                if (item.isMedia()) {
                    deleteMediaItem(item.getId());
                } else if (item.isAlbum()) {
                    deleteAlbumItem(item.getId());
                }
                return true;

                // tải file
            } else if (itemId == R.id.menu_download) {
                // Delete media or album based on item type
                if (item.isMedia()) {
                    Toast.makeText(this, "Download: " + item.getName(), Toast.LENGTH_SHORT).show();

                } else if (item.isAlbum()) {
                    Toast.makeText(this, "Download: " + item.getName(), Toast.LENGTH_SHORT).show();
                }
                return true;


                // chia sẻ backend
            } else if (itemId == R.id.menu_share) {
                shareItem(item);
                return true;

                // chia sẻ email
            } else if (itemId == R.id.menu_email_share) {
                showSendEmailDialog(item);
                return true;
            }
            return false;
        });

        popup.show();
    }

    /**
     * Delete media item by ID
     */
    private void deleteMediaItem(int mediaId) {
        ApiClient.getApiService(this).deleteMedia(mediaId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(HomePage.this,
                                apiResponse.getMessageOrDefault("Media deleted successfully!"),
                                Toast.LENGTH_SHORT).show();

                        // Reload data
                        loadAlbumsAndMedia();
                    } else {
                        Log.e(TAG, "Failed to delete media: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(HomePage.this, "Failed to delete media", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to delete media: " + response.code());
                    Toast.makeText(HomePage.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error deleting media", t);
                Toast.makeText(HomePage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Delete album item by ID
     */
    private void deleteAlbumItem(int albumId) {
        ApiClient.getApiService(this).deleteAlbum(albumId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(HomePage.this,
                                apiResponse.getMessageOrDefault("Album deleted successfully!"),
                                Toast.LENGTH_SHORT).show();

                        // Reload data
                        loadAlbumsAndMedia();
                    } else {
                        Log.e(TAG, "Failed to delete album: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(HomePage.this, "Failed to delete album", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to delete album: " + response.code());
                    Toast.makeText(HomePage.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error deleting album", t);
                Toast.makeText(HomePage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Share item (media or album) with another user
     */
    private void shareItem(FolderItem item) {
        // Create dialog to input receiver email
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_share_item);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Get references to views
        TextView tvShareTitle = dialog.findViewById(R.id.tv_share_title);
        EditText etReceiverEmail = dialog.findViewById(R.id.et_receiver_email);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel_share);
        Button btnShare = dialog.findViewById(R.id.btn_share);

        // Set title
        String itemType = item.isMedia() ? "Media" : "Album";
        tvShareTitle.setText("Share " + itemType + ": " + item.getName());

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Share button
        btnShare.setOnClickListener(v -> {
            String receiverEmail = etReceiverEmail.getText().toString().trim();

            if (receiverEmail.isEmpty()) {
                etReceiverEmail.setError("Please enter receiver email");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(receiverEmail).matches()) {
                etReceiverEmail.setError("Please enter a valid email");
                return;
            }

            // Call API to share
            shareWithUser(item, receiverEmail);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Call API to share resource with user
     */
    private void shareWithUser(FolderItem item, String receiverEmail) {
        String resourceType = item.isMedia() ? "MEDIA" : "ALBUM";
        CreateShareRequest request = new CreateShareRequest(
                resourceType,
                item.getId(),
                "VIEW",  // Default permission
                receiverEmail
        );

        ApiClient.getApiService(this).shareResource(request).enqueue(new Callback<ApiResponse<Share>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Share>> call, @NonNull Response<ApiResponse<Share>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Share> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        Toast.makeText(HomePage.this,
                                apiResponse.getMessageOrDefault("Shared successfully!"),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to share: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(HomePage.this, "Failed to share", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to share: " + response.code());
                    Toast.makeText(HomePage.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Share>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error sharing", t);
                Toast.makeText(HomePage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUploadDialog() {
        // Create the dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_media);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90), // Chiều rộng 90% màn hình
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT // Chiều cao tự động
            );
        }

        // Get references to views in the dialog
        imagePreview = dialog.findViewById(R.id.image_preview);
        Button selectImageButton = dialog.findViewById(R.id.button_select_image);
        EditText captionEditText = dialog.findViewById(R.id.edit_text_caption);
        Button cancelButton = dialog.findViewById(R.id.button_cancel);
        Button uploadButton = dialog.findViewById(R.id.button_upload);

        // Set click listener for the "Select Image" button
        selectImageButton.setOnClickListener(v -> {
            // Tạo một Intent để cho phép chọn cả ảnh và video
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*, video/*"); // Chỉ định cả hai loại MIME

            intent.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            // Mở trình chọn file
            imagePickerLauncher.launch(intent);
        });

        // Set click listener for the "Cancel" button
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Set click listener for the "Upload" button
        uploadButton.setOnClickListener(v -> {
            String caption = captionEditText.getText().toString().trim();
            if (selectedMediaUri == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (caption.isEmpty()) {
                Toast.makeText(this, "Please enter a caption", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start upload process
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            uploadFile(selectedMediaUri, caption);
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    /**
     * Get file name from URI
     */
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Get file size from URI
     */
    private long getFileSize(Uri uri) {
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    size = cursor.getLong(sizeIndex);
                }
            }
        }
        return size;
    }

    /**
     * Step 1: Generate presigned URL
     * Step 2: Upload to S3
     * Step 3: Create media record
     */
    private void uploadFile(Uri fileUri, String caption) {
        try {
            // Get file information
            String fileName = getFileName(fileUri);
            String mimeType = getContentResolver().getType(fileUri);
            long fileSize = getFileSize(fileUri);

            Log.d(TAG, "Uploading file: " + fileName + ", type: " + mimeType + ", size: " + fileSize);

            // Step 1: Generate presigned URL (without albumId for HomePage)
            PresignedUrlRequest request = new PresignedUrlRequest(fileName); // No album

            ApiClient.getApiService(this).generatePresignedUrl(request).enqueue(new Callback<ApiResponse<PresignedUrl>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<PresignedUrl>> call, @NonNull Response<ApiResponse<PresignedUrl>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<PresignedUrl> apiResponse = response.body();

                        if (apiResponse.isSuccess() && apiResponse.hasData()) {
                            PresignedUrl presignedUrl = apiResponse.getData();
                            Log.d(TAG, "Presigned URL generated: " + presignedUrl.getUrl());

                            // Step 2: Upload to S3
                            uploadToS3(fileUri, presignedUrl, fileName, mimeType, fileSize, caption);
                        } else {
                            Log.e(TAG, "Failed to generate presigned URL: " + apiResponse.getMessageOrDefault("Unknown error"));
                            Toast.makeText(HomePage.this, "Failed to generate upload URL", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to generate presigned URL: " + response.code());
                        Toast.makeText(HomePage.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<PresignedUrl>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error generating presigned URL", t);
                    Toast.makeText(HomePage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Step 2: Upload file to S3 using presigned URL
     */
    private void uploadToS3(Uri fileUri, PresignedUrl presignedUrl, String fileName, String mimeType, long fileSize, String caption) {
        new Thread(() -> {
            try {
                // Read file content
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Cannot read file", Toast.LENGTH_SHORT).show());
                    return;
                }

                byte[] fileBytes = new byte[inputStream.available()];
                inputStream.read(fileBytes);
                inputStream.close();

                // Create PUT request to S3
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = RequestBody.create(fileBytes, MediaType.parse(presignedUrl.getContentType()));
                Request request = new Request.Builder()
                        .url(presignedUrl.getUrl())
                        .put(requestBody)
                        .header("Content-Type", presignedUrl.getContentType())
                        .build();

                // Execute upload
                okhttp3.Response s3Response = client.newCall(request).execute();

                if (s3Response.isSuccessful()) {
                    Log.d(TAG, "File uploaded to S3 successfully");

                    // Step 3: Create media record
                    runOnUiThread(() -> createMediaRecord(presignedUrl, fileName, mimeType, fileSize, caption));
                } else {
                    Log.e(TAG, "Failed to upload to S3: " + s3Response.code());
                    runOnUiThread(() -> Toast.makeText(this, "Upload failed: " + s3Response.code(), Toast.LENGTH_SHORT).show());
                }

                s3Response.close();

            } catch (IOException e) {
                Log.e(TAG, "Error uploading to S3", e);
                runOnUiThread(() -> Toast.makeText(this, "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * Step 3: Create media record in database
     */
    private void createMediaRecord(PresignedUrl presignedUrl, String fileName, String mimeType, long fileSize, String caption) {
        // Determine media type based on MIME type
        String mediaType = mimeType.startsWith("image/") ? "IMAGE" : "VIDEO";

        // Build create media request (without albumId for HomePage)
        CreateMediaRequest request = new CreateMediaRequest.Builder()
                .type(mediaType)
                .mimeType(mimeType)
                .filename(fileName)
                .size(fileSize)
                .visibility("PUBLIC")
                .processingStatus("DONE")
                .caption(caption)
                .filePath(presignedUrl.getUploadName())
                .build();

        ApiClient.getApiService(this).createMedia(request).enqueue(new Callback<ApiResponse<Media>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Media>> call, @NonNull Response<ApiResponse<Media>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Media> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        Media media = apiResponse.getData();
                        Log.d(TAG, "Media created successfully: " + media.getFilename());

                        Toast.makeText(HomePage.this, "Upload successful!", Toast.LENGTH_SHORT).show();

                        // Reload media list
                        loadAlbumsAndMedia();
                    } else {
                        Log.e(TAG, "Failed to create media: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(HomePage.this, "Failed to create media record", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to create media: " + response.code());
                    Toast.makeText(HomePage.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Media>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error creating media", t);
                Toast.makeText(HomePage.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSendEmailDialog(FolderItem item) {
        final Dialog emailDialog = new Dialog(this);
        emailDialog.setContentView(R.layout.dialog_send_email);

        // Set chiều rộng cho dialog
        if (emailDialog.getWindow() != null) {
            emailDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.95), // Rộng 95% màn hình
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Lấy view từ layout của dialog
        TextInputEditText toEmailEditText = emailDialog.findViewById(R.id.edit_text_to_email);
        TextInputEditText subjectEditText = emailDialog.findViewById(R.id.edit_text_subject);
        TextInputEditText bodyEditText = emailDialog.findViewById(R.id.edit_text_body);
        Button cancelButton = emailDialog.findViewById(R.id.button_cancel_email);
        Button sendButton = emailDialog.findViewById(R.id.button_send_email);

        // Gán giá trị mặc định (nếu cần)
        subjectEditText.setText("Check out this media: " + item.getName());
        bodyEditText.setText("Hi, I wanted to share this media with you.");

        // Xử lý sự kiện cho nút Cancel
        cancelButton.setOnClickListener(v -> emailDialog.dismiss());

        // Xử lý sự kiện cho nút Send
        sendButton.setOnClickListener(v -> {
            String toEmail = toEmailEditText.getText().toString().trim();
            String subject = subjectEditText.getText().toString().trim();
            String body = bodyEditText.getText().toString().trim();

            if (toEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(toEmail).matches()) {
                toEmailEditText.setError("Please enter a valid email");
                return;
            }
            if (subject.isEmpty()) {
                subjectEditText.setError("Subject cannot be empty");
                return;
            }

//            // 1. Tạo một Intent để gửi email
//            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
//
//            // 2. Thiết lập dữ liệu cho Intent
//            // Dùng "mailto:" để đảm bảo chỉ các ứng dụng email mới xử lý Intent này
//            emailIntent.setData(Uri.parse("mailto:"));
//
//            // 3. Đưa các thông tin cần thiết vào Intent
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ toEmail }); // EXTRA_EMAIL là một mảng String
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
//            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
//
//            // 4. Kiểm tra xem có ứng dụng nào trên điện thoại có thể xử lý Intent này không
//            if (emailIntent.resolveActivity(getPackageManager()) != null) {
//                // Nếu có, bắt đầu Activity (mở ứng dụng email)
//                startActivity(emailIntent);
//                emailDialog.dismiss(); // Đóng dialog của bạn
//            } else {
//                // Nếu không có ứng dụng email nào được cài đặt
//                Toast.makeText(HomePage.this, "No email client found.", Toast.LENGTH_SHORT).show();
//            }

            String fileUrl = item.getMedia().getFilePath();
            if (fileUrl == null || fileUrl.isEmpty()) {
                Toast.makeText(this, "File URL is not available.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "File URL: " + fileUrl);

            emailDialog.dismiss(); // Đóng dialog nhập liệu
            downloadAndShareFile(fileUrl, item.getName(), toEmail, subject, body);


        });

        emailDialog.show();
    }

    private void downloadAndShareFile(String fileUrl, String fileName, String toEmail, String subject, String body) {
        // Hiển thị dialog loading trong khi tải
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Preparing file for sharing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            try {
                // Tạo thư mục cache nếu chưa có
                File cachePath = new File(getCacheDir(), "shared_files");
                cachePath.mkdirs();
                File tempMedia = new File(cachePath, fileName);

                // Tải file bằng OkHttpClient
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(fileUrl).build();
                okhttp3.Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    // Ghi file vào bộ nhớ cache
                    BufferedSink sink = Okio.buffer(Okio.sink(tempMedia));
                    sink.writeAll(response.body().source());
                    sink.close();
                    response.close();

                    // Lấy URI của file đã tải bằng FileProvider (cách làm an toàn nhất)
                    Uri fileUri = FileProvider.getUriForFile(
                            HomePage.this,
                            getApplicationContext().getPackageName() + ".provider", // Phải khớp với authorities trong Manifest
                            tempMedia
                    );

                    // Chạy trên luồng UI để mở Intent
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        shareFileViaIntent(fileUri, toEmail, subject, body);
                    });

                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to download file.", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (IOException e) {
                Log.e(TAG, "Error downloading file for sharing", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    // Thêm phương thức mới này vào HomePage.java

    private void shareFileViaIntent(Uri fileUri, String toEmail, String subject, String body) {
        // Dùng ACTION_SEND để có thể đính kèm file
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        // Xác định loại Intent và loại MIME
        emailIntent.setType(getContentResolver().getType(fileUri));

        // Đặt các thông tin email
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        // Đính kèm file
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // Thêm cờ để cấp quyền đọc URI cho ứng dụng email
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Tạo Chooser để người dùng chọn ứng dụng
        Intent chooser = Intent.createChooser(emailIntent, "Share file via...");

        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "No app found to handle this action.", Toast.LENGTH_SHORT).show();
        }
    }


    // Thêm vào HomePage.java
    private void showEditMediaDialog(FolderItem item, TextView nameTextView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_media);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        TextInputEditText nameEditText = dialog.findViewById(R.id.edit_text_media_name);
        TextInputEditText captionEditText = dialog.findViewById(R.id.edit_text_media_caption);
        Button cancelButton = dialog.findViewById(R.id.button_cancel);
        Button saveButton = dialog.findViewById(R.id.button_save);

        // Điền thông tin hiện tại của file
        Media media = item.getMedia();
        if (media == null) {
            Toast.makeText(this, "Media data not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        nameEditText.setText(media.getFilename());
        captionEditText.setText(media.getCaption());

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newCaption = captionEditText.getText().toString().trim();

            if (newName.isEmpty()) {
                nameEditText.setError("File name cannot be empty");
                return;
            }

            // TODO: Gọi API để cập nhật thông tin media
            // updateMediaOnServer(media.getId(), newName, newCaption);

            item.getMedia().setFilename(newName);
            item.getMedia().setCaption(newCaption);

            nameTextView.setText(newName);

            Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }


    // Thêm vào HomePage.java
    private void showEditAlbumDialog(FolderItem item, TextView nameTextView) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_album);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextInputEditText nameEditText = dialog.findViewById(R.id.edit_text_album_name);
        TextInputEditText descEditText = dialog.findViewById(R.id.edit_text_album_description);
        Button cancelButton = dialog.findViewById(R.id.button_cancel);
        Button saveButton = dialog.findViewById(R.id.button_save);

        // Điền thông tin hiện tại của folder
        Album album = item.getAlbum();
        if (album == null) {
            Toast.makeText(this, "Folder data not found.", Toast.LENGTH_SHORT).show();
            return;
        }
        nameEditText.setText(album.getName());
        descEditText.setText(album.getDescription());

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newDesc = descEditText.getText().toString().trim();

            if (newName.isEmpty()) {
                nameEditText.setError("Folder name cannot be empty");
                return;
            }

            // TODO: Gọi API để cập nhật thông tin album
            // updateAlbumOnServer(album.getId(), newName, newDesc);

            item.getAlbum().setName(newName);
            item.getAlbum().setDescription(newDesc);

            nameTextView.setText(newName);

            Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
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