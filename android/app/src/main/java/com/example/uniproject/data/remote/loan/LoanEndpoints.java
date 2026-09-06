package com.example.uniproject.data.remote.loan;

import com.example.uniproject.data.model.loan.LoanRequest;
import com.example.uniproject.data.model.loan.LoanResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LoanEndpoints {

    @GET("loans")
    Call<List<LoanResponse>> getAll();

    @GET("loans/overdue")
    Call<List<LoanResponse>> getOverdue();

    @GET("loans/{id}")
    Call<LoanResponse> getById(@Path("id") Long id);

    @POST("loans")
    Call<LoanResponse> borrow(@Body LoanRequest request);

    @POST("loans/{id}/return")
    Call<LoanResponse> returnLoan(@Path("id") Long id);
}
