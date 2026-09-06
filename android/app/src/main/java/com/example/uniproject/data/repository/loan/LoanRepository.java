package com.example.uniproject.data.repository.loan;

import androidx.annotation.NonNull;

import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.loan.LoanRequest;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.loan.LoanEndpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class LoanRepository {
    private final LoanEndpoints loanEndpoints;

    public LoanRepository() {
        loanEndpoints = RetrofitProvider.getRetrofit().create(LoanEndpoints.class);
    }

    public Call<List<LoanResponse>> getAll(LoanListCallback callback) {
        Call<List<LoanResponse>> call = loanEndpoints.getAll();
        enqueueList(call, callback);
        return call;
    }

    public Call<List<LoanResponse>> getOverdue(LoanListCallback callback) {
        Call<List<LoanResponse>> call = loanEndpoints.getOverdue();
        enqueueList(call, callback);
        return call;
    }

    public Call<LoanResponse> getById(Long id, LoanCallback callback) {
        Call<LoanResponse> call = loanEndpoints.getById(id);
        enqueueLoan(call, callback);
        return call;
    }

    public Call<LoanResponse> borrow(
            LoanRequest request,
            LoanCallback callback
    ) {
        Call<LoanResponse> call = loanEndpoints.borrow(request);
        enqueueLoan(call, callback);
        return call;
    }

    public Call<LoanResponse> returnLoan(Long id, LoanCallback callback) {
        Call<LoanResponse> call = loanEndpoints.returnLoan(id);
        enqueueLoan(call, callback);
        return call;
    }

    private void enqueueList(
            Call<List<LoanResponse>> call,
            LoanListCallback callback
    ) {
        call.enqueue(new Callback<List<LoanResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<LoanResponse>> call,
                    @NonNull Response<List<LoanResponse>> response
            ) {
                List<LoanResponse> loans = response.body();
                if (!response.isSuccessful() || loans == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(loans);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<LoanResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    private void enqueueLoan(
            Call<LoanResponse> call,
            LoanCallback callback
    ) {
        call.enqueue(new Callback<LoanResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<LoanResponse> call,
                    @NonNull Response<LoanResponse> response
            ) {
                LoanResponse loan = response.body();
                if (!response.isSuccessful() || loan == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(loan);
            }

            @Override
            public void onFailure(
                    @NonNull Call<LoanResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    public interface LoanListCallback {
        void onSuccess(List<LoanResponse> loans);

        void onError(ApiErrorResponse error);
    }

    public interface LoanCallback {
        void onSuccess(LoanResponse loan);

        void onError(ApiErrorResponse error);
    }
}
