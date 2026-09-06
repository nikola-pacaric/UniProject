package com.example.uniproject.data.remote.member;

import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberRequest;
import com.example.uniproject.data.model.member.MemberResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MemberEndpoints {

    @GET("members")
    Call<List<MemberResponse>> getAll();

    @GET("members/{id}")
    Call<MemberResponse> getById(@Path("id") Long id);

    @GET("members/{id}/loans")
    Call<List<LoanResponse>> getLoanHistory(@Path("id") Long id);

    @POST("members")
    Call<MemberResponse> create(@Body MemberRequest request);

    @PUT("members/{id}")
    Call<MemberResponse> update(
            @Path("id") Long id,
            @Body MemberRequest request
    );

    @PATCH("members/{id}/activate")
    Call<MemberResponse> activate(@Path("id") Long id);

    @PATCH("members/{id}/deactivate")
    Call<MemberResponse> deactivate(@Path("id") Long id);
}
