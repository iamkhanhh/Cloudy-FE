package com.example.cloudstorage.api;

import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.ChangePasswordRequest;
import com.example.cloudstorage.models.LoginRequest;
import com.example.cloudstorage.models.LoginResponse;
import com.example.cloudstorage.models.ResendOtpRequest;
import com.example.cloudstorage.models.User;
import com.example.cloudstorage.models.VerifyAccountRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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
     * POST /auth/activate-account/:id
     * Kích hoạt tài khoản với mã OTP
     *
     * @param id userId nhận từ email hoặc sau khi register
     * @param request chứa code (OTP)
     * @return ApiResponse với status và message
     */
    @POST("auth/activate-account/{id}")
    Call<ApiResponse> activateAccount(
            @Path("id") int id,
            @Body VerifyAccountRequest request
    );

    /**
     * POST /auth/resend-otp
     * Gửi lại mã OTP
     *
     * @param request chứa email
     * @return ApiResponse với status và message
     */
    @POST("auth/resend-otp")
    Call<ApiResponse> resendOtp(@Body ResendOtpRequest request);

    /**
     * PUT /auth/change-password
     * Đổi mật khẩu
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * @param request chứa currentPassword và newPassword
     * @return ApiResponse với status và message
     */
    @PUT("auth/change-password")
    Call<ApiResponse> changePassword(@Body ChangePasswordRequest request);

    /**
     * POST /logout (nếu backend có endpoint này)
     */
    // @POST("logout")
    // Call<Void> logout();
}
