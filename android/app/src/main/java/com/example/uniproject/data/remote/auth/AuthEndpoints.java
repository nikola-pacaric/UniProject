package com.example.uniproject.data.remote.auth;

import com.example.uniproject.data.model.auth.AuthResponse;
import com.example.uniproject.data.model.auth.LoginRequest;
import com.example.uniproject.data.model.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthEndpoints {

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);
}
