package com.example.uniproject.data.repository.book;

import androidx.annotation.NonNull;

import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.book.BookRequest;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.book.BookEndpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class BookRepository {
    private final BookEndpoints bookEndpoints;

    public BookRepository() {
        bookEndpoints = RetrofitProvider.getRetrofit().create(BookEndpoints.class);
    }

    public Call<List<BookResponse>> getAll(BookListCallback callback) {
        Call<List<BookResponse>> call = bookEndpoints.getAll();
        enqueueList(call, callback);
        return call;
    }

    public Call<List<BookResponse>> search(
            String query,
            BookListCallback callback
    ) {
        Call<List<BookResponse>> call = bookEndpoints.search(query);
        enqueueList(call, callback);
        return call;
    }

    public Call<BookResponse> create(
            BookRequest request,
            BookMutationCallback callback
    ) {
        Call<BookResponse> call = bookEndpoints.create(request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<BookResponse> update(
            Long id,
            BookRequest request,
            BookMutationCallback callback
    ) {
        Call<BookResponse> call = bookEndpoints.update(id, request);
        enqueueMutation(call, callback);
        return call;
    }

    public Call<Void> delete(Long id, BookDeleteCallback callback) {
        Call<Void> call = bookEndpoints.delete(id);
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

    private void enqueueList(
            Call<List<BookResponse>> call,
            BookListCallback callback
    ) {
        call.enqueue(new Callback<List<BookResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<BookResponse>> call,
                    @NonNull Response<List<BookResponse>> response
            ) {
                List<BookResponse> books = response.body();
                if (!response.isSuccessful() || books == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(books);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<BookResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    private void enqueueMutation(
            Call<BookResponse> call,
            BookMutationCallback callback
    ) {
        call.enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<BookResponse> call,
                    @NonNull Response<BookResponse> response
            ) {
                BookResponse book = response.body();
                if (!response.isSuccessful() || book == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(book);
            }

            @Override
            public void onFailure(
                    @NonNull Call<BookResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    public interface BookListCallback {
        void onSuccess(List<BookResponse> books);

        void onError(ApiErrorResponse error);
    }

    public interface BookMutationCallback {
        void onSuccess(BookResponse book);

        void onError(ApiErrorResponse error);
    }

    public interface BookDeleteCallback {
        void onSuccess();

        void onError(ApiErrorResponse error);
    }
}
