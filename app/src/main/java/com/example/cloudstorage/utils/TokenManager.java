package com.example.cloudstorage.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TokenManager sử dụng SharedPreferences để lưu trữ token
 * SharedPreferences lưu dữ liệu vào file XML trong bộ nhớ internal storage của app
 * Dữ liệu sẽ được BẢO TOÀN khi:
 * - User thoát app (kill app)
 * - User khởi động lại thiết bị
 * - App bị crash
 *
 * Dữ liệu chỉ bị XÓA khi:
 * - User uninstall app
 * - User clear app data trong Settings
 * - App gọi hàm clearToken()
 */
public class TokenManager {
    private static final String PREF_NAME = "CloudyAuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        // MODE_PRIVATE: chỉ app này mới truy cập được
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Lưu access token vào SharedPreferences
     * Token sẽ được lưu vĩnh viễn cho đến khi xóa hoặc uninstall app
     */
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply(); // apply() là async, commit() là sync
    }

    /**
     * Lấy access token từ SharedPreferences
     * @return token nếu có, null nếu không có hoặc đã hết hạn
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Lưu email của user (optional - để hiển thị UI)
     */
    public void saveUserEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    /**
     * Lấy email của user
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Xóa token và tất cả thông tin đã lưu
     * Gọi hàm này khi:
     * - User logout
     * - Token hết hạn (401 Unauthorized)
     */
    public void clearToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Kiểm tra xem user đã đăng nhập chưa
     * @return true nếu có token, false nếu không
     */
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
