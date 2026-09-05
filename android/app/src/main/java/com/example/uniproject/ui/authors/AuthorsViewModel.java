package com.example.uniproject.ui.authors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.author.AuthorRequest;
import com.example.uniproject.data.model.author.AuthorResponse;
import com.example.uniproject.data.repository.author.AuthorRepository;

import java.util.List;

import retrofit2.Call;

public final class AuthorsViewModel extends ViewModel {
    private final AuthorRepository authorRepository = new AuthorRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<AuthorResponse>> authors = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> creating = new MutableLiveData<>(false);
    private final MutableLiveData<AuthorResponse> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> createError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updating = new MutableLiveData<>(false);
    private final MutableLiveData<AuthorResponse> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> updateError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<AuthorResponse> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> deleteError = new MutableLiveData<>();

    private Call<List<AuthorResponse>> activeListCall;
    private Call<AuthorResponse> activeCreateCall;
    private Call<AuthorResponse> activeUpdateCall;
    private Call<Void> activeDeleteCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<AuthorResponse>> getAuthors() {
        return authors;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public LiveData<Boolean> getCreating() {
        return creating;
    }

    public LiveData<AuthorResponse> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<ApiErrorResponse> getCreateError() {
        return createError;
    }

    public LiveData<Boolean> getUpdating() {
        return updating;
    }

    public LiveData<AuthorResponse> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<ApiErrorResponse> getUpdateError() {
        return updateError;
    }

    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    public LiveData<AuthorResponse> getDeleteSuccess() {
        return deleteSuccess;
    }

    public LiveData<ApiErrorResponse> getDeleteError() {
        return deleteError;
    }

    public void loadAuthors() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        loadError.setValue(null);
        activeListCall = authorRepository.getAll(new AuthorRepository.AuthorListCallback() {
            @Override
            public void onSuccess(List<AuthorResponse> loadedAuthors) {
                authors.postValue(loadedAuthors);
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

    public void createAuthor(String firstName, String lastName, String biography) {
        if (isMutationInProgress()) {
            return;
        }

        creating.setValue(true);
        createSuccess.setValue(null);
        createError.setValue(null);

        AuthorRequest request = new AuthorRequest(firstName, lastName, biography);
        activeCreateCall = authorRepository.create(
                request,
                new AuthorRepository.AuthorMutationCallback() {
                    @Override
                    public void onSuccess(AuthorResponse author) {
                        creating.postValue(false);
                        createSuccess.postValue(author);
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

    public void updateAuthor(
            Long id,
            String firstName,
            String lastName,
            String biography
    ) {
        if (isMutationInProgress()) {
            return;
        }

        updating.setValue(true);
        updateSuccess.setValue(null);
        updateError.setValue(null);

        AuthorRequest request = new AuthorRequest(firstName, lastName, biography);
        activeUpdateCall = authorRepository.update(
                id,
                request,
                new AuthorRepository.AuthorMutationCallback() {
                    @Override
                    public void onSuccess(AuthorResponse author) {
                        updating.postValue(false);
                        updateSuccess.postValue(author);
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

    public void deleteAuthor(AuthorResponse author) {
        if (isMutationInProgress() || author.getId() == null) {
            return;
        }

        deleting.setValue(true);
        deleteSuccess.setValue(null);
        deleteError.setValue(null);

        activeDeleteCall = authorRepository.delete(
                author.getId(),
                new AuthorRepository.AuthorDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        deleting.postValue(false);
                        deleteSuccess.postValue(author);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        deleting.postValue(false);
                        deleteError.postValue(error);
                    }
                }
        );
    }

    public void consumeDeleteSuccess() {
        deleteSuccess.setValue(null);
    }

    public void consumeDeleteError() {
        deleteError.setValue(null);
    }

    private boolean isMutationInProgress() {
        return Boolean.TRUE.equals(creating.getValue())
                || Boolean.TRUE.equals(updating.getValue())
                || Boolean.TRUE.equals(deleting.getValue());
    }

    private void clearMutationResults() {
        createSuccess.setValue(null);
        createError.setValue(null);
        updateSuccess.setValue(null);
        updateError.setValue(null);
        deleteSuccess.setValue(null);
        deleteError.setValue(null);
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
        if (activeDeleteCall != null) {
            activeDeleteCall.cancel();
        }
        super.onCleared();
    }
}
