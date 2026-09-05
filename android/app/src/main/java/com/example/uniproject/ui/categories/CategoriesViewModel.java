package com.example.uniproject.ui.categories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.category.CategoryRequest;
import com.example.uniproject.data.model.category.CategoryResponse;
import com.example.uniproject.data.repository.category.CategoryRepository;

import java.util.List;

import retrofit2.Call;

public final class CategoriesViewModel extends ViewModel {
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<CategoryResponse>> categories = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> creating = new MutableLiveData<>(false);
    private final MutableLiveData<CategoryResponse> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> createError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updating = new MutableLiveData<>(false);
    private final MutableLiveData<CategoryResponse> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> updateError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<CategoryResponse> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> deleteError = new MutableLiveData<>();

    private Call<List<CategoryResponse>> activeListCall;
    private Call<CategoryResponse> activeCreateCall;
    private Call<CategoryResponse> activeUpdateCall;
    private Call<Void> activeDeleteCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<CategoryResponse>> getCategories() {
        return categories;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public LiveData<Boolean> getCreating() {
        return creating;
    }

    public LiveData<CategoryResponse> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<ApiErrorResponse> getCreateError() {
        return createError;
    }

    public LiveData<Boolean> getUpdating() {
        return updating;
    }

    public LiveData<CategoryResponse> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<ApiErrorResponse> getUpdateError() {
        return updateError;
    }

    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    public LiveData<CategoryResponse> getDeleteSuccess() {
        return deleteSuccess;
    }

    public LiveData<ApiErrorResponse> getDeleteError() {
        return deleteError;
    }

    public void loadCategories() {
        if (Boolean.TRUE.equals(loading.getValue())) {
            return;
        }

        loading.setValue(true);
        loadError.setValue(null);
        activeListCall = categoryRepository.getAll(new CategoryRepository.CategoryListCallback() {
            @Override
            public void onSuccess(List<CategoryResponse> loadedCategories) {
                categories.postValue(loadedCategories);
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

    public void createCategory(String name, String description) {
        if (isMutationInProgress()) {
            return;
        }

        creating.setValue(true);
        createSuccess.setValue(null);
        createError.setValue(null);

        CategoryRequest request = new CategoryRequest(name, description);
        activeCreateCall = categoryRepository.create(
                request,
                new CategoryRepository.CategoryMutationCallback() {
                    @Override
                    public void onSuccess(CategoryResponse category) {
                        creating.postValue(false);
                        createSuccess.postValue(category);
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

    public void updateCategory(Long id, String name, String description) {
        if (isMutationInProgress()) {
            return;
        }

        updating.setValue(true);
        updateSuccess.setValue(null);
        updateError.setValue(null);

        CategoryRequest request = new CategoryRequest(name, description);
        activeUpdateCall = categoryRepository.update(
                id,
                request,
                new CategoryRepository.CategoryMutationCallback() {
                    @Override
                    public void onSuccess(CategoryResponse category) {
                        updating.postValue(false);
                        updateSuccess.postValue(category);
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

    public void deleteCategory(CategoryResponse category) {
        if (isMutationInProgress() || category.getId() == null) {
            return;
        }

        deleting.setValue(true);
        deleteSuccess.setValue(null);
        deleteError.setValue(null);

        activeDeleteCall = categoryRepository.delete(
                category.getId(),
                new CategoryRepository.CategoryDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        deleting.postValue(false);
                        deleteSuccess.postValue(category);
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
