package com.example.uniproject.data.repository.author;

import androidx.annotation.NonNull;

import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.author.AuthorRequest;
import com.example.uniproject.data.model.author.AuthorResponse;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.author.AuthorEndpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AuthorRepository {
    private final AuthorEndpoints authorEndpoints;

    public AuthorRepository() {
        authorEndpoints = RetrofitProvider.getRetrofit().create(AuthorEndpoints.class);
    }

    public Call<List<AuthorResponse>> getAll(AuthorListCallback callback) {
        Call<List<AuthorResponse>> call = authorEndpoints.getAll();
        call.enqueue(new Callback<List<AuthorResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<AuthorResponse>> call,
                    @NonNull Response<List<AuthorResponse>> response
            ) {
                List<AuthorResponse> authors = response.body();
                if (!response.isSuccessful() || authors == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(authors);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<AuthorResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
        return call;
    }

    public Call<AuthorResponse> create(
            AuthorRequest request,
            AuthorMutationCallback callback
    ) {
        Call<AuthorResponse> call = authorEndpoints.create(request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<AuthorResponse> update(
            Long id,
            AuthorRequest request,
            AuthorMutationCallback callback
    ) {
        Call<AuthorResponse> call = authorEndpoints.update(id, request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<Void> delete(Long id, AuthorDeleteCallback callback) {
        Call<Void> call = authorEndpoints.delete(id);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }
                callback.onSuccess();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
        return call;
    }

    private void enqueueMutation(
            Call<AuthorResponse> call,
            AuthorMutationCallback callback
    ) {
        call.enqueue(new Callback<AuthorResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<AuthorResponse> call,
                    @NonNull Response<AuthorResponse> response
            ) {
                AuthorResponse author = response.body();
                if (!response.isSuccessful() || author == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(author);
            }

            @Override
            public void onFailure(
                    @NonNull Call<AuthorResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    public interface AuthorListCallback {
        void onSuccess(List<AuthorResponse> authors);

        void onError(ApiErrorResponse error);
    }

    public interface AuthorMutationCallback {
        void onSuccess(AuthorResponse author);

        void onError(ApiErrorResponse error);
    }

    public interface AuthorDeleteCallback {
        void onSuccess();

        void onError(ApiErrorResponse error);
    }
}
