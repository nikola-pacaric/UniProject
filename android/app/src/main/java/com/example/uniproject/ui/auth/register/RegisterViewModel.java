package com.example.uniproject.ui.auth.register;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.auth.AuthResponse;
import com.example.uniproject.data.model.auth.RegisterRequest;
import com.example.uniproject.data.repository.auth.AuthRepository;

import retrofit2.Call;

public final class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<AuthResponse> registrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> registrationError = new MutableLiveData<>();

    private Call<AuthResponse> activeCall;

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<AuthResponse> getRegistrationSuccess() {
        return registrationSuccess;
    }

    public LiveData<ApiErrorResponse> getRegistrationError() {
        return registrationError;
    }

    public void consumeRegistrationSuccess() {
        registrationSuccess.setValue(null);
    }

    public void register(String username, String email, String password, String fullName) {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        registrationError.setValue(null);
        registrationSuccess.setValue(null);

        RegisterRequest request = new RegisterRequest(username, email, password, fullName);
        activeCall = authRepository.register(request, new AuthRepository.AuthResultCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                loading.postValue(false);
                registrationSuccess.postValue(response);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                loading.postValue(false);
                registrationError.postValue(error);
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
