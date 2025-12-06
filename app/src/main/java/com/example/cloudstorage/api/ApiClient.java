package com.example.cloudstorage.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ApiClient - Singleton class để tạo và quản lý Retrofit instance
 */
public class ApiClient {
    private static final String BASE_URL = "http://localhost:3000/api/v1";

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    /**
     * Lấy Retrofit instance (Singleton pattern)
     */
    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            // Logging interceptor để debug (xem request/response trong Logcat)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // BODY, HEADERS, BASIC, NONE

            // OkHttpClient với interceptors
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context)) // Thêm token vào header
                    .addInterceptor(loggingInterceptor)            // Log request/response
                    .connectTimeout(30, TimeUnit.SECONDS)          // Timeout kết nối
                    .readTimeout(30, TimeUnit.SECONDS)             // Timeout đọc data
                    .writeTimeout(30, TimeUnit.SECONDS)            // Timeout ghi data
                    .build();

            // Tạo Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create()) // JSON converter
                    .build();
        }
        return retrofit;
    }

    /**
     * Lấy ApiService instance
     * Sử dụng hàm này để gọi API
     *
     * Example:
     * ApiClient.getApiService(context).login(loginRequest).enqueue(callback);
     */
    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            apiService = getRetrofitInstance(context).create(ApiService.class);
        }
        return apiService;
    }

    /**
     * Reset Retrofit instance (dùng khi cần refresh configuration)
     */
    public static void resetClient() {
        retrofit = null;
        apiService = null;
    }
}
