package com.example.uniproject.data.repository.category;

import androidx.annotation.NonNull;

import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.category.CategoryRequest;
import com.example.uniproject.data.model.category.CategoryResponse;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.category.CategoryEndpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class CategoryRepository {
    private final CategoryEndpoints categoryEndpoints;

    public CategoryRepository() {
        categoryEndpoints = RetrofitProvider.getRetrofit().create(CategoryEndpoints.class);
    }

    public Call<List<CategoryResponse>> getAll(CategoryListCallback callback) {
        Call<List<CategoryResponse>> call = categoryEndpoints.getAll();
        call.enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<CategoryResponse>> call,
                    @NonNull Response<List<CategoryResponse>> response
            ) {
                List<CategoryResponse> categories = response.body();
                if (!response.isSuccessful() || categories == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(categories);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<CategoryResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
        return call;
    }

    public Call<CategoryResponse> create(
            CategoryRequest request,
            CategoryMutationCallback callback
    ) {
        Call<CategoryResponse> call = categoryEndpoints.create(request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<CategoryResponse> update(
            Long id,
            CategoryRequest request,
            CategoryMutationCallback callback
    ) {
        Call<CategoryResponse> call = categoryEndpoints.update(id, request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<Void> delete(Long id, CategoryDeleteCallback callback) {
        Call<Void> call = categoryEndpoints.delete(id);
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
            Call<CategoryResponse> call,
            CategoryMutationCallback callback
    ) {
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<CategoryResponse> call,
                    @NonNull Response<CategoryResponse> response
            ) {
                CategoryResponse category = response.body();
                if (!response.isSuccessful() || category == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(category);
            }

            @Override
            public void onFailure(
                    @NonNull Call<CategoryResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    public interface CategoryListCallback {
        void onSuccess(List<CategoryResponse> categories);

        void onError(ApiErrorResponse error);
    }

    public interface CategoryMutationCallback {
        void onSuccess(CategoryResponse category);

        void onError(ApiErrorResponse error);
    }

    public interface CategoryDeleteCallback {
        void onSuccess();

        void onError(ApiErrorResponse error);
    }
}
