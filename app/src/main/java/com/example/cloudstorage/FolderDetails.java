package com.example.cloudstorage;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.Album;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.CreateMediaRequest;
import com.example.cloudstorage.models.CreateShareRequest;
import com.example.cloudstorage.models.Media;
import com.example.cloudstorage.models.PresignedUrl;
import com.example.cloudstorage.models.PresignedUrlRequest;
import com.example.cloudstorage.models.Share;
import com.google.android.flexbox.FlexboxLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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
    private Uri selectedMediaUri;
    private ImageView imagePreview;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedMediaUri = result.getData().getData();
                if (imagePreview != null && selectedMediaUri != null) {
                    String mimeType = getContentResolver().getType(selectedMediaUri);

                    if (mimeType != null && mimeType.startsWith("video")) {
                        Glide.with(this)
                                .load(selectedMediaUri)
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(imagePreview);
                    } else {
                        Glide.with(this)
                                .load(selectedMediaUri)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(imagePreview);
                    }
                }
            }
        });

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

        // Setup back button
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Initialize views
        tvAlbumName = findViewById(R.id.tv_album_name);
        tvAlbumDescription = findViewById(R.id.tv_album_description);
        foldersListLayout = findViewById(R.id.folderslist);
        mediaList = new ArrayList<>();

        // Setup upload button
        ImageView btnAddMedia = findViewById(R.id.btn_add_media);
        btnAddMedia.setOnClickListener(v -> showUploadDialog());

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
                        .placeholder(R.drawable.ic_images)
                        .error(R.drawable.ic_images)
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
        popup.getMenuInflater().inflate(R.menu.menu_media_options, popup.getMenu());

        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_edit) {
                Toast.makeText(this, "Edit: " + media.getFilename(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_delete) {
                deleteMediaItem(media.getId());
                return true;
            } else if (itemId == R.id.menu_share) {
                shareItem(media);
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
                        Toast.makeText(FolderDetails.this,
                                apiResponse.getMessageOrDefault("Media deleted successfully!"),
                                Toast.LENGTH_SHORT).show();

                        // Reload data
                        loadAlbumMedia();
                    } else {
                        Log.e(TAG, "Failed to delete media: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(FolderDetails.this, "Failed to delete media", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to delete media: " + response.code());
                    Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error deleting media", t);
                Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareItem(Media item) {
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
        String itemType = "Media";
        tvShareTitle.setText("Share " + itemType + ": " + item.getFilename());

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
    private void shareWithUser(Media item, String receiverEmail) {
        String resourceType = "MEDIA";
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
                        Toast.makeText(FolderDetails.this,
                                apiResponse.getMessageOrDefault("Shared successfully!"),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to share: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(FolderDetails.this, "Failed to share", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to share: " + response.code());
                    Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Share>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error sharing", t);
                Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUploadDialog() {
        // Create the dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_media);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        imagePreview = dialog.findViewById(R.id.image_preview);
        Button selectImageButton = dialog.findViewById(R.id.button_select_image);
        EditText captionEditText = dialog.findViewById(R.id.edit_text_caption);
        Button cancelButton = dialog.findViewById(R.id.button_cancel);
        Button uploadButton = dialog.findViewById(R.id.button_upload);

        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*, video/*");

            intent.setType("*/*");
            String[] mimeTypes = {"image/*", "video/*"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

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

            // Step 1: Generate presigned URL
            PresignedUrlRequest request = new PresignedUrlRequest(fileName, albumId);

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
                            Toast.makeText(FolderDetails.this, "Failed to generate upload URL", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Failed to generate presigned URL: " + response.code());
                        Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<PresignedUrl>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error generating presigned URL", t);
                    Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                .albumsId(albumId)
                .build();

        ApiClient.getApiService(this).createMedia(request).enqueue(new Callback<ApiResponse<Media>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Media>> call, @NonNull Response<ApiResponse<Media>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Media> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.hasData()) {
                        Media media = apiResponse.getData();
                        Log.d(TAG, "Media created successfully: " + media.getFilename());

                        Toast.makeText(FolderDetails.this, "Upload successful!", Toast.LENGTH_SHORT).show();

                        // Reload media list
                        loadAlbumMedia();
                    } else {
                        Log.e(TAG, "Failed to create media: " + apiResponse.getMessageOrDefault("Unknown error"));
                        Toast.makeText(FolderDetails.this, "Failed to create media record", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to create media: " + response.code());
                    Toast.makeText(FolderDetails.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Media>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error creating media", t);
                Toast.makeText(FolderDetails.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}