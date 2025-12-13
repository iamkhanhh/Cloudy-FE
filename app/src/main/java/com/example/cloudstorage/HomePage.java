package com.example.cloudstorage;

import android.content.Intent;
import android.os.Bundle;
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
import android.view.MenuItem;


import com.bumptech.glide.Glide;
import com.example.cloudstorage.utils.TokenManager;


public class HomePage extends AppCompatActivity {
    private TokenManager tokenManager;

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
                            // Xử lý khi nhấn "Edit"
                            Toast.makeText(HomePage.this, "Edit clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.menu_delete) {
                            // Xử lý khi nhấn "Delete"
                            Toast.makeText(HomePage.this, "Delete clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (itemId == R.id.menu_share) {
                            // Xử lý khi nhấn "Share"
                            Toast.makeText(HomePage.this, "Share clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

                // Hiển thị menu
                popup.show();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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