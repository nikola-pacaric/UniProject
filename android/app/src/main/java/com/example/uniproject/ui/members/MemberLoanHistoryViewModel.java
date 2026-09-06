package com.example.uniproject.ui.members;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.example.uniproject.data.repository.member.MemberRepository;

import java.util.List;

import retrofit2.Call;

public final class MemberLoanHistoryViewModel extends ViewModel {
    private final MemberRepository memberRepository = new MemberRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<MemberResponse> member = new MutableLiveData<>();
    private final MutableLiveData<List<LoanResponse>> loans = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();

    private Long currentMemberId;
    private int pendingRequests;
    private Call<MemberResponse> activeMemberCall;
    private Call<List<LoanResponse>> activeHistoryCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<MemberResponse> getMember() {
        return member;
    }

    public LiveData<List<LoanResponse>> getLoans() {
        return loans;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public Long getCurrentMemberId() {
        return currentMemberId;
    }

    public void loadHistory(Long memberId) {
        if (memberId == null || memberId <= 0L) {
            return;
        }
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        currentMemberId = memberId;
        loading.setValue(true);
        loadError.setValue(null);
        pendingRequests = 2;

        activeMemberCall = memberRepository.getById(
                memberId,
                new MemberRepository.MemberCallback() {
                    @Override
                    public void onSuccess(MemberResponse loadedMember) {
                        member.postValue(loadedMember);
                        finishRequest(null);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        finishRequest(error);
                    }
                }
        );

        activeHistoryCall = memberRepository.getLoanHistory(
                memberId,
                new MemberRepository.MemberLoanHistoryCallback() {
                    @Override
                    public void onSuccess(List<LoanResponse> loadedLoans) {
                        loans.postValue(loadedLoans);
                        finishRequest(null);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        finishRequest(error);
                    }
                }
        );
    }

    public void reloadHistory() {
        loadHistory(currentMemberId);
    }

    private void finishRequest(ApiErrorResponse error) {
        if (error != null) {
            loadError.postValue(error);
        }

        pendingRequests--;
        if (pendingRequests <= 0) {
            loading.postValue(false);
        }
    }

    @Override
    protected void onCleared() {
        if (activeMemberCall != null) {
            activeMemberCall.cancel();
        }
        if (activeHistoryCall != null) {
            activeHistoryCall.cancel();
        }
        super.onCleared();
    }
}
