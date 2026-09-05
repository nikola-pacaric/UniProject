package com.example.uniproject.data.remote.author;

import com.example.uniproject.data.model.author.AuthorRequest;
import com.example.uniproject.data.model.author.AuthorResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthorEndpoints {
    @GET("authors")
    Call<List<AuthorResponse>> getAll();

    @POST("authors")
    Call<AuthorResponse> create(@Body AuthorRequest request);

    @PUT("authors/{id}")
    Call<AuthorResponse> update(@Path("id") Long id, @Body AuthorRequest request);

    @DELETE("authors/{id}")
    Call<Void> delete(@Path("id") Long id);
}
