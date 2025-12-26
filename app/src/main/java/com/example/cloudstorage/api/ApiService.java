package com.example.cloudstorage.api;

import com.example.cloudstorage.models.Album;
import com.example.cloudstorage.models.ApiResponse;
import com.example.cloudstorage.models.ChangePasswordRequest;
import com.example.cloudstorage.models.CreateAlbumRequest;
import com.example.cloudstorage.models.CreateMediaRequest;
import com.example.cloudstorage.models.CreateShareRequest;
import com.example.cloudstorage.models.LoginRequest;
import com.example.cloudstorage.models.LoginResponse;
import com.example.cloudstorage.models.Media;
import com.example.cloudstorage.models.PresignedUrl;
import com.example.cloudstorage.models.PresignedUrlRequest;
import com.example.cloudstorage.models.RegisterRequest;
import com.example.cloudstorage.models.RegisterResponse;
import com.example.cloudstorage.models.ReportRequest;
import com.example.cloudstorage.models.ResendOtpRequest;
import com.example.cloudstorage.models.Share;
import com.example.cloudstorage.models.StorageData;
import com.example.cloudstorage.models.User;
import com.example.cloudstorage.models.VerifyAccountRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
     * POST /auth/register
     * Đăng ký tài khoản mới
     *
     * Backend response: { "status": "success", "message": "...", "data": { "id": x, "email": "..." } }
     *
     * @param registerRequest chứa email, password, first_name, last_name
     * @return ApiResponse wrapping RegisterResponse (id and email)
     */
    @POST("auth/register")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest registerRequest);

    /**
     * GET /auth/me
     * Lấy thông tin user hiện tại
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": { ...user object... } }
     *
     * @return ApiResponse wrapping User object
     */
    @GET("auth/me")
    Call<ApiResponse<User>> getProfile();

    /**
     * POST /auth/activate-account/:id
     * Kích hoạt tài khoản với mã OTP
     *
     * Backend response: { "status": "success", "message": "..." }
     *
     * @param id userId nhận từ email hoặc sau khi register
     * @param request chứa code (OTP)
     * @return ApiResponse with no data (Void)
     */
    @POST("auth/activate-account/{id}")
    Call<ApiResponse<Void>> activateAccount(
            @Path("id") int id,
            @Body VerifyAccountRequest request
    );

    /**
     * POST /media/generate-single-presigned-url
     * Generate presigned url
     *
     * Backend response: { "status": "success", "message": "...", "data": { "id": x, "email": "..." } }
     *
     * @param request 
     * @return ApiResponse PresignedUrl
     */
    @POST("media/generate-single-presigned-url")
    Call<ApiResponse<PresignedUrl>> generatePresignedUrl(@Body PresignedUrlRequest request);

    /**
     * POST /auth/resend-otp
     * Gửi lại mã OTP
     *
     * Backend response: { "status": "success", "message": "...", "data": { "id": x, "email": "..." } }
     * Note: Data contains id and email but we don't currently use it
     *
     * @param request chứa email
     * @return ApiResponse with no data (Void) - data field not used
     */
    @POST("auth/resend-otp")
    Call<ApiResponse<Void>> resendOtp(@Body ResendOtpRequest request);

    /**
     * PUT /auth/change-password
     * Đổi mật khẩu
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "..." }
     *
     * @param request chứa currentPassword và newPassword
     * @return ApiResponse with no data (Void)
     */
    @PUT("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    /**
     * POST /users/report
     * Gửi báo cáo help/bug report
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "..." }
     *
     * @param request chứa title và details
     * @return ApiResponse with no data (Void)
     */
    @POST("users/report")
    Call<ApiResponse<Void>> submitReport(@Body ReportRequest request);

    /**
     * GET /users/storage
     * Lấy thông tin dung lượng lưu trữ
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": { "IMAGE": x, "VIDEO": y, "TOTAL": z } }
     *
     * @return ApiResponse wrapping StorageData (IMAGE, VIDEO, TOTAL in GB)
     */
    @GET("users/storage")
    Call<ApiResponse<StorageData>> getStorage();

    /**
     * GET /media/{id}
     * Lấy thông tin chi tiết media (ảnh hoặc video) theo ID
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": { ...media object... } }
     *
     * @param id Media ID
     * @return ApiResponse wrapping Media object
     */
    @GET("media/{id}")
    Call<ApiResponse<Media>> getMediaById(@Path("id") int id);

    /**
     * PATCH /media/{id}
     * Update chi tiết media (ảnh hoặc video) theo ID
     *
     * Backend response: { "status": "success"}
     *
     * @param id Media ID
     * @param request chứa thông tin media (filename, caption)
     * @return ApiResponse wrapping Media object
     */
    @PATCH("media/{id}")
    Call<ApiResponse<Void>> updateMediaById(@Path("id") int id, @Body CreateMediaRequest request);

    /**
     * GET /media
     * Lấy danh sách tất cả media của user hiện tại
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "...", "data": [ ...media objects... ] }
     *
     * @return ApiResponse wrapping List of Media objects
     */
    @GET("media")
    Call<ApiResponse<List<Media>>> getAllMedia();

    /**
     * GET /albums
     * Lấy danh sách tất cả albums của user hiện tại
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": [ ...album objects... ] }
     *
     * @return ApiResponse wrapping List of Album objects
     */
    @GET("albums")
    Call<ApiResponse<List<Album>>> getAllAlbums();

    /**
     * GET /albums/{id}
     * Lấy thông tin chi tiết album theo ID
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": { ...album object... } }
     *
     * @param id Album ID
     * @return ApiResponse wrapping Album object
     */
    @GET("albums/{id}")
    Call<ApiResponse<Album>> getAlbumById(@Path("id") int id);

    /**
     * GET /media?albumId={albumId}
     * Lấy danh sách media thuộc một album cụ thể
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": [ ...media objects... ] }
     *
     * @param albumId Album ID để filter media
     * @return ApiResponse wrapping List of Media objects
     */
    @GET("media")
    Call<ApiResponse<List<Media>>> getMediaByAlbumId(@Query("albumId") int albumId);

    /**
     * POST /media/download/{id}
     * Lấy link download media (ảnh hoặc video) theo ID
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": { ...media object... } }
     *
     * @param id Media ID
     * @return ApiResponse wrapping Media object
     */
    @POST("media/download/{id}")
    Call<ApiResponse<PresignedUrl>> getDownloadMedia(@Path("id") int id);

    /**
     * POST /media
     * Tạo media record mới sau khi upload file lên S3
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "Media created successfully", "data": { ...media object... } }
     *
     * @param createMediaRequest chứa thông tin media (type, mime_type, filename, size, file_path, etc.)
     * @return ApiResponse wrapping Media object
     */
    @POST("media")
    Call<ApiResponse<Media>> createMedia(@Body CreateMediaRequest createMediaRequest);

    /**
     * GET /shares
     * Lấy danh sách tất cả media và albums được chia sẻ với user hiện tại
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "data": [ ...share objects... ] }
     * Each share contains:
     *   - resource_type: "MEDIA" or "ALBUM"
     *   - content: actual Media or Album object
     *
     * @return ApiResponse wrapping List of Share objects
     */
    @GET("shares")
    Call<ApiResponse<List<Share>>> getSharedItems();

    /**
     * POST /shares
     * Chia sẻ media hoặc album với người dùng khác qua email
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "Share created successfully", "data": { ...share object... } }
     *
     * @param createShareRequest chứa resource_type, resource_id, permission, receiver_email
     * @return ApiResponse wrapping Share object
     */
    @POST("shares")
    Call<ApiResponse<Share>> shareResource(@Body CreateShareRequest createShareRequest);

    /**
     * PATCH /albums/{id}
     * Update chi tiết album theo ID
     *
     * Backend response: { "status": "success"}
     *
     * @param id Media ID
     * @param request chứa thông tin media (filename, caption)
     * @return ApiResponse wrapping Media object
     */
    @PATCH("albums/{id}")
    Call<ApiResponse<Void>> updateAlbumById(@Path("id") int id, @Body CreateAlbumRequest request);

    /**
     * POST /albums
     * Tao album moi
     *
     * Backend response: { "status": "success"}
     *
     * @return ApiResponse wrapping Media object
     */
    @POST("albums")
    Call<ApiResponse<Album>> createAlbum(@Body CreateAlbumRequest request);

    /**
     * DELETE /media/{id}
     * Xóa media theo ID (soft delete)
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "Media deleted successfully" }
     *
     * @param id Media ID cần xóa
     * @return ApiResponse with no data (Void)
     */
    @DELETE("media/{id}")
    Call<ApiResponse<Void>> deleteMedia(@Path("id") int id);

    /**
     * DELETE /albums/{id}
     * Xóa album theo ID (soft delete)
     * Authorization header sẽ tự động được thêm bởi AuthInterceptor
     *
     * Backend response: { "status": "success", "message": "Album deleted successfully" }
     *
     * @param id Album ID cần xóa
     * @return ApiResponse with no data (Void)
     */
    @DELETE("albums/{id}")
    Call<ApiResponse<Void>> deleteAlbum(@Path("id") int id);

    /**
     * POST /logout (nếu backend có endpoint này)
     */
    // @POST("logout")
    // Call<Void> logout();
}
