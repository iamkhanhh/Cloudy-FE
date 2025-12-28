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

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest registerRequest);

    @GET("auth/me")
    Call<ApiResponse<User>> getProfile();

    @POST("auth/activate-account/{id}")
    Call<ApiResponse<Void>> activateAccount(
            @Path("id") int id,
            @Body VerifyAccountRequest request
    );

    @POST("media/generate-single-presigned-url")
    Call<ApiResponse<PresignedUrl>> generatePresignedUrl(@Body PresignedUrlRequest request);

    @POST("auth/resend-otp")
    Call<ApiResponse<Void>> resendOtp(@Body ResendOtpRequest request);

    @PUT("auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    @POST("users/report")
    Call<ApiResponse<Void>> submitReport(@Body ReportRequest request);

    @GET("users/storage")
    Call<ApiResponse<StorageData>> getStorage();

    @GET("media/{id}")
    Call<ApiResponse<Media>> getMediaById(@Path("id") int id);

    @PATCH("media/{id}")
    Call<ApiResponse<Void>> updateMediaById(@Path("id") int id, @Body CreateMediaRequest request);

    @GET("media")
    Call<ApiResponse<List<Media>>> getAllMedia();

    @GET("albums")
    Call<ApiResponse<List<Album>>> getAllAlbums();

    @GET("albums/{id}")
    Call<ApiResponse<Album>> getAlbumById(@Path("id") int id);

    @GET("media")
    Call<ApiResponse<List<Media>>> getMediaByAlbumId(@Query("albumId") int albumId);

    @POST("media/download/{id}")
    Call<ApiResponse<PresignedUrl>> getDownloadMedia(@Path("id") int id);

    @POST("media")
    Call<ApiResponse<Media>> createMedia(@Body CreateMediaRequest createMediaRequest);

    @GET("shares")
    Call<ApiResponse<List<Share>>> getSharedItems();

    @POST("shares")
    Call<ApiResponse<Share>> shareResource(@Body CreateShareRequest createShareRequest);

    @PATCH("albums/{id}")
    Call<ApiResponse<Void>> updateAlbumById(@Path("id") int id, @Body CreateAlbumRequest request);

    @POST("albums")
    Call<ApiResponse<Album>> createAlbum(@Body CreateAlbumRequest request);

    @DELETE("media/{id}")
    Call<ApiResponse<Void>> deleteMedia(@Path("id") int id);

    @DELETE("albums/{id}")
    Call<ApiResponse<Void>> deleteAlbum(@Path("id") int id);
}
