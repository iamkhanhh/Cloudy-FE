package com.example.cloudstorage;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MenuItem;


import com.bumptech.glide.Glide;
import com.example.cloudstorage.utils.TokenManager;
import com.google.android.material.textfield.TextInputEditText;


public class HomePage extends AppCompatActivity {
    private TokenManager tokenManager;

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

        tokenManager = new TokenManager(this);
        DrawerLayout drawerLayout =  findViewById(R.id.main);
        ImageView menuIcon = findViewById(R.id.menu_icon);
        TextView btn_nav_profile = findViewById(R.id.nav_profile_text);
        TextView nav_logout_text = findViewById(R.id.nav_logout_text);

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

        // Find the ImageView by its ID
//        ImageView imageView = findViewById(R.id.folder_image_1);
//
//        // Define the image URL
//        String imageUrl = "https://media-cdn-v2.laodong.vn/Storage/NewsPortal/2020/2/21/785984/D.jpg";
//
//        // Use Glide to load the image from the URL into the ImageView
//        Glide.with(this).load(imageUrl).into(imageView);

        // Tìm ImageView của bạn bằng ID (ví dụ cho folder đầu tiên)
// Hãy chắc chắn rằng bạn đã đặt ID này trong file XML
        ImageView folderOptionsMenu = findViewById(R.id.folder1_options_menu);

        folderOptionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override    public void onClick(View view) {
                // Tạo một đối tượng PopupMenu
                PopupMenu popup = new PopupMenu(HomePage.this, view);
                // "Thổi phồng" (inflate) file menu của bạn vào PopupMenu
                popup.getMenuInflater().inflate(R.menu.folder_option_menu, popup.getMenu());

                // Đặt một listener để xử lý khi một mục trong menu được chọn
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        if (itemId == R.id.menu_edit) {
                            // Handle "Edit" click
                            Toast.makeText(HomePage.this, "Edit clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.menu_delete) {
                            // Handle "Delete" click
                            Toast.makeText(HomePage.this, "Delete clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.menu_share) {
                            // --- GỌI DIALOG TẠI ĐÂY ---
                            showSendEmailDialog();
                            return true;
                        }
                        return false;
                    }

                });

                // Hiển thị menu
                popup.show();
            }
        });

        // Find the plus button
        addFileButton = findViewById(R.id.add_file_button);

        // Set the click listener
        addFileButton.setOnClickListener(v -> showUploadDialog());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Upload dialog
     */
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

            // --- Your Upload Logic Goes Here ---
            // You now have the 'selectedMediaUri' and 'caption'.
            // You can use these to upload the file using Retrofit or another library.
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            // For example: uploadFile(selectedMediaUri, caption);

            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }



    private void showSendEmailDialog() {
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
        subjectEditText.setText("Check out this media");
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

            // --- GỌI API GỬI MAIL CỦA BẠN TẠI ĐÂY ---
            // Ví dụ:
            // YourApiClass.sendEmail(toEmail, subject, body, new Callback<...>() { ... });

            Toast.makeText(HomePage.this, "Sending email to " + toEmail, Toast.LENGTH_SHORT).show();

            emailDialog.dismiss(); // Đóng dialog sau khi gửi
        });

        emailDialog.show();
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