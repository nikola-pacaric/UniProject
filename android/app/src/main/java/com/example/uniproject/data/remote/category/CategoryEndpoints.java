package com.example.uniproject.data.remote.category;

import com.example.uniproject.data.model.category.CategoryRequest;
import com.example.uniproject.data.model.category.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CategoryEndpoints {
    @GET("categories")
    Call<List<CategoryResponse>> getAll();

    @POST("categories")
    Call<CategoryResponse> create(@Body CategoryRequest request);

    @PUT("categories/{id}")
    Call<CategoryResponse> update(
            @Path("id") Long id,
            @Body CategoryRequest request
    );

    @DELETE("categories/{id}")
    Call<Void> delete(@Path("id") Long id);
}
