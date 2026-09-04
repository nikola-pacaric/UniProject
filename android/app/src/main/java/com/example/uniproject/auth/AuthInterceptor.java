package com.example.uniproject.auth;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class AuthInterceptor implements Interceptor {
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        if (isPublicAuthRequest(request)) {
            return chain.proceed(request);
        }

        String token = sessionManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(request);
        }

        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(authenticatedRequest);
    }

    private boolean isPublicAuthRequest(Request request) {
        String path = request.url().encodedPath();
        return LOGIN_PATH.equals(path) || REGISTER_PATH.equals(path);
    }
}
