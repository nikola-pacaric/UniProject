package com.example.uniproject.data.remote;

import android.content.Context;

import com.example.uniproject.auth.AuthInterceptor;
import com.example.uniproject.auth.SessionManager;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitProvider {
    private static final String BASE_URL = "http://10.0.2.2:8000/api/";

    private static volatile Retrofit retrofit;

    private RetrofitProvider() {
    }

    public static synchronized void initialize(Context context) {
        if (retrofit != null) {
            return;
        }

        SessionManager sessionManager = new SessionManager(context);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            throw new IllegalStateException("RetrofitProvider must be initialized first.");
        }

        return retrofit;
    }
}
