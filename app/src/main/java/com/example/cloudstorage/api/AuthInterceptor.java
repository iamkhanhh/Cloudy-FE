package com.example.cloudstorage.api;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.cloudstorage.MainActivity;
import com.example.cloudstorage.utils.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * AuthInterceptor tự động:
 * 1. Thêm Authorization header vào mọi request
 * 2. Kiểm tra response 401 (Unauthorized) để redirect về login
 */
public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token từ TokenManager
        String token = tokenManager.getToken();

        // Nếu có token, thêm vào Authorization header
        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        // Thêm các header khác nếu cần
        requestBuilder.addHeader("Content-Type", "application/json");
        requestBuilder.addHeader("Accept", "application/json");

        Request request = requestBuilder.build();
        Response response = chain.proceed(request);

        // Kiểm tra nếu server trả về 401 Unauthorized
        if (response.code() == 401) {
            handleUnauthorized();
        }

        return response;
    }

    /**
     * Xử lý khi token hết hạn hoặc không hợp lệ
     * 1. Xóa token cũ
     * 2. Chuyển về màn hình login
     * 3. Hiển thị thông báo cho user
     */
    private void handleUnauthorized() {
        // Xóa token cũ
        tokenManager.clearToken();

        // Chuyển về màn hình login
        // FLAG_ACTIVITY_NEW_TASK: tạo task mới
        // FLAG_ACTIVITY_CLEAR_TASK: xóa tất cả activity cũ trong stack
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("session_expired", true);
        context.startActivity(intent);

        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() ->
            Toast.makeText(context, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show()
        );
    }
}
