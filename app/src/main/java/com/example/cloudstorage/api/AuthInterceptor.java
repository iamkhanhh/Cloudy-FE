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

        String token = tokenManager.getToken();

        Request.Builder requestBuilder = originalRequest.newBuilder();
        if (token != null && !token.isEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer " + token);
        }

        requestBuilder.addHeader("Content-Type", "application/json");
        requestBuilder.addHeader("Accept", "application/json");

        Request request = requestBuilder.build();
        Response response = chain.proceed(request);

        if (response.code() == 401) {
            handleUnauthorized();
        }

        return response;
    }

    private void handleUnauthorized() {
        tokenManager.clearToken();

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("session_expired", true);
        context.startActivity(intent);

        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.post(() ->
            Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_LONG).show()
        );
    }
}
