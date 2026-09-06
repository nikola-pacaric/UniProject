package com.example.uniproject.data.repository.member;

import androidx.annotation.NonNull;

import com.example.uniproject.data.http.ApiErrorMapper;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberRequest;
import com.example.uniproject.data.model.member.MemberResponse;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.data.remote.member.MemberEndpoints;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class MemberRepository {
    private final MemberEndpoints memberEndpoints;

    public MemberRepository() {
        memberEndpoints = RetrofitProvider.getRetrofit().create(MemberEndpoints.class);
    }

    public Call<List<MemberResponse>> getAll(MemberListCallback callback) {
        Call<List<MemberResponse>> call = memberEndpoints.getAll();
        call.enqueue(new Callback<List<MemberResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<MemberResponse>> call,
                    @NonNull Response<List<MemberResponse>> response
            ) {
                List<MemberResponse> members = response.body();
                if (!response.isSuccessful() || members == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(members);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<MemberResponse>> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
        return call;
    }

    public Call<MemberResponse> getById(Long id, MemberCallback callback) {
        Call<MemberResponse> call = memberEndpoints.getById(id);
        enqueueMember(call, callback);
        return call;
    }

    public Call<List<LoanResponse>> getLoanHistory(
            Long memberId,
            MemberLoanHistoryCallback callback
    ) {
        Call<List<LoanResponse>> call = memberEndpoints.getLoanHistory(memberId);
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
        return call;
    }

    public Call<MemberResponse> create(
            MemberRequest request,
            MemberCallback callback
    ) {
        Call<MemberResponse> call = memberEndpoints.create(request);
        enqueueMember(call, callback);
        return call;
    }

    public Call<MemberResponse> update(
            Long id,
            MemberRequest request,
            MemberCallback callback
    ) {
        Call<MemberResponse> call = memberEndpoints.update(id, request);
        enqueueMember(call, callback);
        return call;
    }

    public Call<MemberResponse> activate(Long id, MemberCallback callback) {
        Call<MemberResponse> call = memberEndpoints.activate(id);
        enqueueMember(call, callback);
        return call;
    }

    public Call<MemberResponse> deactivate(Long id, MemberCallback callback) {
        Call<MemberResponse> call = memberEndpoints.deactivate(id);
        enqueueMember(call, callback);
        return call;
    }

    private void enqueueMember(
            Call<MemberResponse> call,
            MemberCallback callback
    ) {
        call.enqueue(new Callback<MemberResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<MemberResponse> call,
                    @NonNull Response<MemberResponse> response
            ) {
                MemberResponse member = response.body();
                if (!response.isSuccessful() || member == null) {
                    callback.onError(ApiErrorMapper.fromResponse(response));
                    return;
                }

                callback.onSuccess(member);
            }

            @Override
            public void onFailure(
                    @NonNull Call<MemberResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorMapper.fromThrowable(throwable));
                }
            }
        });
    }

    public interface MemberListCallback {
        void onSuccess(List<MemberResponse> members);

        void onError(ApiErrorResponse error);
    }

    public interface MemberCallback {
        void onSuccess(MemberResponse member);

        void onError(ApiErrorResponse error);
    }

    public interface MemberLoanHistoryCallback {
        void onSuccess(List<LoanResponse> loans);

        void onError(ApiErrorResponse error);
    }
}
