package com.example.uniproject.data.remote.book;

import com.example.uniproject.data.model.book.BookRequest;
import com.example.uniproject.data.model.book.BookResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface BookEndpoints {

    @GET("books")
    Call<List<BookResponse>> getAll();

    @GET("books/search")
    Call<List<BookResponse>> search(@Query("q") String query);

    @GET("books/{id}")
    Call<BookResponse> getById(@Path("id") Long id);

    @POST("books")
    Call<BookResponse> create(@Body BookRequest request);

    @PUT("books/{id}")
    Call<BookResponse> update(
            @Path("id") Long id,
            @Body BookRequest request
    );

    @DELETE("books/{id}")
    Call<Void> delete(@Path("id") Long id);
}
