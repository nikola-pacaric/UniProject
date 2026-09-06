package com.example.uniproject.ui.members;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.member.MemberRequest;
import com.example.uniproject.data.model.member.MemberResponse;
import com.example.uniproject.data.repository.member.MemberRepository;

import java.util.List;

import retrofit2.Call;

public final class MembersViewModel extends ViewModel {
    private final MemberRepository memberRepository = new MemberRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<MemberResponse>> members = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> creating = new MutableLiveData<>(false);
    private final MutableLiveData<MemberResponse> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> createError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updating = new MutableLiveData<>(false);
    private final MutableLiveData<MemberResponse> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> updateError = new MutableLiveData<>();
    private final MutableLiveData<Long> statusChangingMemberId = new MutableLiveData<>();
    private final MutableLiveData<MemberResponse> statusChangeSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> statusChangeError = new MutableLiveData<>();

    private Call<List<MemberResponse>> activeListCall;
    private Call<MemberResponse> activeCreateCall;
    private Call<MemberResponse> activeUpdateCall;
    private Call<MemberResponse> activeStatusCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<MemberResponse>> getMembers() {
        return members;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public LiveData<Boolean> getCreating() {
        return creating;
    }

    public LiveData<MemberResponse> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<ApiErrorResponse> getCreateError() {
        return createError;
    }

    public LiveData<Boolean> getUpdating() {
        return updating;
    }

    public LiveData<MemberResponse> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<ApiErrorResponse> getUpdateError() {
        return updateError;
    }

    public LiveData<Long> getStatusChangingMemberId() {
        return statusChangingMemberId;
    }

    public LiveData<MemberResponse> getStatusChangeSuccess() {
        return statusChangeSuccess;
    }

    public LiveData<ApiErrorResponse> getStatusChangeError() {
        return statusChangeError;
    }

    public void loadMembers() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        loadError.setValue(null);
        activeListCall = memberRepository.getAll(new MemberRepository.MemberListCallback() {
            @Override
            public void onSuccess(List<MemberResponse> loadedMembers) {
                members.postValue(loadedMembers);
                loading.postValue(false);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                loadError.postValue(error);
                loading.postValue(false);
            }
        });
    }

    public void prepareCreate() {
        if (isMutationInProgress()) {
            return;
        }
        clearMutationResults();
    }

    public void createMember(
            String firstName,
            String lastName,
            String membershipCardNumber,
            String email,
            String phone
    ) {
        if (isMutationInProgress()) {
            return;
        }

        creating.setValue(true);
        createSuccess.setValue(null);
        createError.setValue(null);

        MemberRequest request = new MemberRequest(
                firstName,
                lastName,
                membershipCardNumber,
                email,
                phone
        );
        activeCreateCall = memberRepository.create(
                request,
                new MemberRepository.MemberCallback() {
                    @Override
                    public void onSuccess(MemberResponse member) {
                        creating.postValue(false);
                        createSuccess.postValue(member);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        creating.postValue(false);
                        createError.postValue(error);
                    }
                }
        );
    }

    public void consumeCreateSuccess() {
        createSuccess.setValue(null);
    }

    public void prepareUpdate() {
        if (isMutationInProgress()) {
            return;
        }
        clearMutationResults();
    }

    public void updateMember(
            Long id,
            String firstName,
            String lastName,
            String membershipCardNumber,
            String email,
            String phone
    ) {
        if (isMutationInProgress() || id == null) {
            return;
        }

        updating.setValue(true);
        updateSuccess.setValue(null);
        updateError.setValue(null);

        MemberRequest request = new MemberRequest(
                firstName,
                lastName,
                membershipCardNumber,
                email,
                phone
        );
        activeUpdateCall = memberRepository.update(
                id,
                request,
                new MemberRepository.MemberCallback() {
                    @Override
                    public void onSuccess(MemberResponse member) {
                        updating.postValue(false);
                        updateSuccess.postValue(member);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        updating.postValue(false);
                        updateError.postValue(error);
                    }
                }
        );
    }

    public void consumeUpdateSuccess() {
        updateSuccess.setValue(null);
    }

    public void activateMember(MemberResponse member) {
        if (member == null || member.getId() == null || Boolean.TRUE.equals(member.getActive())) {
            return;
        }
        changeMemberStatus(member.getId(), true);
    }

    public void deactivateMember(MemberResponse member) {
        if (member == null || member.getId() == null || !Boolean.TRUE.equals(member.getActive())) {
            return;
        }
        changeMemberStatus(member.getId(), false);
    }

    public void consumeStatusChangeSuccess() {
        statusChangeSuccess.setValue(null);
    }

    public void consumeStatusChangeError() {
        statusChangeError.setValue(null);
    }

    private void changeMemberStatus(Long id, boolean activate) {
        if (isMutationInProgress()) {
            return;
        }

        statusChangingMemberId.setValue(id);
        statusChangeSuccess.setValue(null);
        statusChangeError.setValue(null);

        MemberRepository.MemberCallback callback = new MemberRepository.MemberCallback() {
            @Override
            public void onSuccess(MemberResponse member) {
                statusChangingMemberId.postValue(null);
                statusChangeSuccess.postValue(member);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                statusChangingMemberId.postValue(null);
                statusChangeError.postValue(error);
            }
        };

        activeStatusCall = activate
                ? memberRepository.activate(id, callback)
                : memberRepository.deactivate(id, callback);
    }

    private boolean isMutationInProgress() {
        return Boolean.TRUE.equals(creating.getValue())
                || Boolean.TRUE.equals(updating.getValue())
                || statusChangingMemberId.getValue() != null;
    }

    private void clearMutationResults() {
        createSuccess.setValue(null);
        createError.setValue(null);
        updateSuccess.setValue(null);
        updateError.setValue(null);
        statusChangeSuccess.setValue(null);
        statusChangeError.setValue(null);
    }

    @Override
    protected void onCleared() {
        if (activeListCall != null) {
            activeListCall.cancel();
        }
        if (activeCreateCall != null) {
            activeCreateCall.cancel();
        }
        if (activeUpdateCall != null) {
            activeUpdateCall.cancel();
        }
        if (activeStatusCall != null) {
            activeStatusCall.cancel();
        }
        super.onCleared();
    }
}
