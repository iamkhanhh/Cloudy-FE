package com.example.cloudstorage.api;

import com.example.cloudstorage.models.LoginRequest;
import com.example.cloudstorage.models.LoginResponse;
import com.example.cloudstorage.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Interface định nghĩa các API endpoints
 * Retrofit sẽ tự động generate implementation code
 */
public interface ApiService {

    /**
     * POST /auth/login
     * Đăng nhập và nhận access_token
     *
     * @param loginRequest chứa email và password
     * @return LoginResponse chứa access_token
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * GET /auth/me
     * Lấy thông tin user hiện tại
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * @return User object
     */
    @GET("auth/me")
    Call<User> getProfile();

    /**
     * POST /logout
     */
    // @POST("logout")
    // Call<Void> logout();

    // Thêm các API endpoints khác ở đây
    // Ví dụ:
    // @GET("files")
    // Call<List<File>> getFiles();
    //
    // @POST("files/upload")
    // Call<File> uploadFile(@Body RequestBody file);
}
