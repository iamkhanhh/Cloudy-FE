package com.example.cloudstorage;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cloudstorage.api.ApiClient;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.StorageData;
import com.example.cloudstorage.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class storage_details extends AppCompatActivity {

    // UI Components
    private ProgressBar storageProgressBar;
    private TextView usedSpaceText;
    private TextView totalSpaceText;

    // Images item
    private View itemImages;
    private ImageView imageIcon;
    private TextView imageCategoryName;
    private TextView imageFileCount;
    private TextView imageStorageSize;

    // Videos item
    private View itemVideos;
    private ImageView videoIcon;
    private TextView videoCategoryName;
    private TextView videoFileCount;
    private TextView videoStorageSize;

    // Token manager
    private TokenManager tokenManager;

    // Storage capacity (assumed to be 50 GB for now)
    private static final double MAX_STORAGE_GB = 50.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_storage_details);

        tokenManager = new TokenManager(this);

        initViews();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        loadStorageData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        storageProgressBar = findViewById(R.id.storage_progressbar);
        usedSpaceText = findViewById(R.id.used_space_text);
        totalSpaceText = findViewById(R.id.total_space_text);

        // Images item
        itemImages = findViewById(R.id.item_images);
        imageIcon = itemImages.findViewById(R.id.item_icon);
        imageCategoryName = itemImages.findViewById(R.id.item_category_name);
        imageFileCount = itemImages.findViewById(R.id.item_file_count);
        imageStorageSize = itemImages.findViewById(R.id.item_storage_size);

        // Videos item
        itemVideos = findViewById(R.id.item_videos);
        videoIcon = itemVideos.findViewById(R.id.item_icon);
        videoCategoryName = itemVideos.findViewById(R.id.item_category_name);
        videoFileCount = itemVideos.findViewById(R.id.item_file_count);
        videoStorageSize = itemVideos.findViewById(R.id.item_storage_size);

        // Set default values for category names
        imageCategoryName.setText("Images");
        videoCategoryName.setText("Videos");

        // Set icons (assuming you have these drawables)
        imageIcon.setImageResource(R.drawable.ic_images);
        videoIcon.setImageResource(R.drawable.ic_video);

        imageFileCount.setVisibility(View.GONE);
        videoFileCount.setVisibility(View.GONE);
    }

    private void loadStorageData() {
        ApiClient.getApiService(this).getStorage()
                .enqueue(new Callback<ApiResponse<StorageData>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<StorageData>> call, @NonNull Response<ApiResponse<StorageData>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<StorageData> apiResponse = response.body();

                            if (apiResponse.getData() != null) {
                                StorageData storageData = apiResponse.getData();
                                updateUI(storageData);
                            } else {
                                Toast.makeText(storage_details.this, "No storage data", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(storage_details.this, "Cannot load storage information", Toast.LENGTH_SHORT).show();

                            if (response.code() == 401) {
                                tokenManager.clearToken();
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<StorageData>> call, @NonNull Throwable t) {
                        Toast.makeText(storage_details.this, "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        t.printStackTrace();
                    }
                });
    }

    private void updateUI(StorageData storageData) {
        double totalUsed = storageData.getTotal();
        double imageSize = storageData.getImage();
        double videoSize = storageData.getVideo();

        // Update used space text
        usedSpaceText.setText(String.format("%.2f GB", totalUsed));
        totalSpaceText.setText(String.format("Used of %.0f GB", MAX_STORAGE_GB));

        // Calculate and update progress bar (max = 1000 for smooth animation)
        int progressPercentage = (int) ((totalUsed / MAX_STORAGE_GB) * 1000);
        storageProgressBar.setProgress(progressPercentage);

        // Update Images storage size
        imageStorageSize.setText(formatStorageSize(imageSize));

        // Update Videos storage size
        videoStorageSize.setText(formatStorageSize(videoSize));
    }

    /**
     * Format storage size to display
     * Converts GB to appropriate unit (MB if less than 0.01 GB)
     */
    private String formatStorageSize(double sizeInGB) {
        if (sizeInGB < 0.01) {
            // Convert to MB if very small
            double sizeInMB = sizeInGB * 1024;
            return String.format("%.1f MB", sizeInMB);
        } else {
            return String.format("%.2f GB", sizeInGB);
        }
    }
}
