package com.example.uniproject.ui.auth.login;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.auth.AuthResponse;
import com.example.uniproject.data.model.auth.LoginRequest;
import com.example.uniproject.data.repository.auth.AuthRepository;

import retrofit2.Call;

public final class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<AuthResponse> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loginError = new MutableLiveData<>();

    private Call<AuthResponse> activeCall;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<AuthResponse> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<ApiErrorResponse> getLoginError() {
        return loginError;
    }

    public void login(String username, String password) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        loginError.setValue(null);

        LoginRequest request = new LoginRequest(username, password);
        activeCall = authRepository.login(request, new AuthRepository.AuthResultCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                loading.postValue(false);
                loginSuccess.postValue(response);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                loading.postValue(false);
                loginError.postValue(error);
            }
        });
    }

    @Override
    protected void onCleared() {
        if (activeCall != null) {
            activeCall.cancel();
        }
        super.onCleared();
    }
}
