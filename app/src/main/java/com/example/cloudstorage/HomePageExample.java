//package com.example.cloudstorage;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.drawerlayout.widget.DrawerLayout;
//
//import com.example.cloudstorage.api.ApiClient;
//import com.example.cloudstorage.models.User;
//import com.example.cloudstorage.utils.TokenManager;
//import com.google.android.material.navigation.NavigationView;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
///**
// * HomePage Example với Retrofit
// * Ví dụ về cách:
// * 1. Gọi API /profile với Authorization header (tự động thêm bởi AuthInterceptor)
// * 2. Xử lý logout
// * 3. Hiển thị thông tin user
// */
//public class HomePageExample extends AppCompatActivity {
//
//    private TokenManager tokenManager;
//    private TextView userNameTextView;
//    private TextView userEmailTextView;
//    private DrawerLayout drawerLayout;
//    private NavigationView navigationView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_home_page);
//
//        // Khởi tạo TokenManager
//        tokenManager = new TokenManager(this);
//
//        // Kiểm tra nếu chưa login, chuyển về MainActivity
//        if (!tokenManager.isLoggedIn()) {
//            navigateToLogin();
//            return;
//        }
//
//        // Tìm views
//        // userNameTextView = findViewById(R.id.userName); // Thay bằng ID thực
//        // userEmailTextView = findViewById(R.id.userEmail); // Thay bằng ID thực
//        drawerLayout = findViewById(R.id.drawer_layout);
//        navigationView = findViewById(R.id.nav_view);
//
//        // Load user profile từ API
//        loadUserProfile();
//
//        // Xử lý navigation menu
//        setupNavigationMenu();
//    }
//
//    /**
//     * Gọi API /profile để lấy thông tin user
//     */
//    private void loadUserProfile() {
//        // Authorization header sẽ tự động được thêm bởi AuthInterceptor
//        ApiClient.getApiService(this).getProfile().enqueue(new Callback<User>() {
//            @Override
//            public void onResponse(Call<User> call, Response<User> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    User user = response.body();
//
//                    // Hiển thị thông tin user
//                    displayUserInfo(user);
//
//                } else {
//                    Toast.makeText(HomePageExample.this, "Không thể tải thông tin user", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<User> call, Throwable t) {
//                Toast.makeText(HomePageExample.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                t.printStackTrace();
//            }
//        });
//    }
//
//    /**
//     * Hiển thị thông tin user lên UI
//     */
//    private void displayUserInfo(User user) {
//        if (userNameTextView != null) {
//            userNameTextView.setText(user.getName());
//        }
//        if (userEmailTextView != null) {
//            userEmailTextView.setText(user.getEmail());
//        }
//
//        // Cập nhật navigation header
//        if (navigationView != null) {
//            View headerView = navigationView.getHeaderView(0);
//            TextView navUserName = headerView.findViewById(R.id.nav_user_name);
//            TextView navUserEmail = headerView.findViewById(R.id.nav_user_email);
//
//            if (navUserName != null) navUserName.setText(user.getName());
//            if (navUserEmail != null) navUserEmail.setText(user.getEmail());
//        }
//
//        // Có thể lưu thêm thông tin user vào SharedPreferences nếu cần
//    }
//
//    /**
//     * Thiết lập Navigation Menu
//     */
//    private void setupNavigationMenu() {
//        if (navigationView != null) {
//            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//                @Override
//                public boolean onNavigationItemSelected(MenuItem item) {
//                    int itemId = item.getItemId();
//
//                    // Xử lý các menu items
//                    if (itemId == R.id.nav_home) {
//                        // Đã ở trang Home
//                        drawerLayout.closeDrawers();
//                        return true;
//
//                    } else if (itemId == R.id.nav_profile) {
//                        // Chuyển đến Profile
//                        Intent intent = new Intent(HomePageExample.this, my_profiile.class);
//                        startActivity(intent);
//                        return true;
//
//                    } else if (itemId == R.id.nav_storage) {
//                        // Chuyển đến Storage Details
//                        Intent intent = new Intent(HomePageExample.this, storage_details.class);
//                        startActivity(intent);
//                        return true;
//
//                    } else if (itemId == R.id.nav_settings) {
//                        // Chuyển đến Settings
//                        Toast.makeText(HomePageExample.this, "Settings", Toast.LENGTH_SHORT).show();
//                        return true;
//
//                    } else if (itemId == R.id.nav_help) {
//                        // Chuyển đến Help
//                        Intent intent = new Intent(HomePageExample.this, help_report.class);
//                        startActivity(intent);
//                        return true;
//
//                    } else if (itemId == R.id.nav_logout) {
//                        // Logout
//                        handleLogout();
//                        return true;
//                    }
//
//                    return false;
//                }
//            });
//        }
//    }
//
//    /**
//     * Xử lý logout
//     */
//    private void handleLogout() {
//        // Option 1: Chỉ xóa token local (đơn giản)
//        tokenManager.clearToken();
//        navigateToLogin();
//
//        // Option 2: Gọi API logout backend (nếu có)
//        /*
//        ApiClient.getApiService(this).logout().enqueue(new Callback<Void>() {
//            @Override
//            public void onResponse(Call<Void> call, Response<Void> response) {
//                tokenManager.clearToken();
//                navigateToLogin();
//            }
//
//            @Override
//            public void onFailure(Call<Void> call, Throwable t) {
//                // Vẫn logout local dù API thất bại
//                tokenManager.clearToken();
//                navigateToLogin();
//            }
//        });
//        */
//    }
//
//    /**
//     * Chuyển về màn hình login
//     */
//    private void navigateToLogin() {
//        Intent intent = new Intent(HomePageExample.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }
//}
