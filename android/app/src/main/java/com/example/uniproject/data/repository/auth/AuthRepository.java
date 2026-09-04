package com.example.uniproject.data.repository.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.uniproject.auth.SessionManager;
import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.auth.AuthResponse;
import com.example.uniproject.data.model.auth.LoginRequest;
import com.example.uniproject.data.model.auth.RegisterRequest;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.auth.AuthEndpoints;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AuthRepository {
    private final AuthEndpoints authEndpoints;
    private final SessionManager sessionManager;

    public AuthRepository(Context context) {
        authEndpoints = RetrofitProvider.getRetrofit().create(AuthEndpoints.class);
        sessionManager = new SessionManager(context);
    }

    public Call<AuthResponse> login(LoginRequest request, AuthResultCallback callback) {
        Call<AuthResponse> call = authEndpoints.login(request);
        enqueue(call, true, callback);
        return call;
    }

    public Call<AuthResponse> register(RegisterRequest request, AuthResultCallback callback) {
        Call<AuthResponse> call = authEndpoints.register(request);
        enqueue(call, false, callback);
        return call;
    }

    private void enqueue(
            Call<AuthResponse> call,
            boolean saveSession,
            AuthResultCallback callback
    ) {
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Response<AuthResponse> response
            ) {
                AuthResponse authResponse = response.body();

                if (!response.isSuccessful() || authResponse == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                if (saveSession) {
                    String token = authResponse.getToken();
                    if (token == null || token.trim().isEmpty()) {
                        callback.onError(missingTokenError());
                        return;
                    }

                    sessionManager.saveSession(authResponse);
                }

                callback.onSuccess(authResponse);
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    private ApiErrorResponse missingTokenError() {
        ApiErrorResponse error = new ApiErrorResponse();
        error.setStatus(0);
        error.setMessage("Server nije vratio token za prijavu.");
        return error;
    }

    public interface AuthResultCallback {
        void onSuccess(AuthResponse response);

        void onError(ApiErrorResponse error);
    }
}
