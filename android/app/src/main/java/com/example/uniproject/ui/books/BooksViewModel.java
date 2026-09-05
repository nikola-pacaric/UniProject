package com.example.uniproject.ui.books;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.author.AuthorResponse;
import com.example.uniproject.data.model.book.BookRequest;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.model.category.CategoryResponse;
import com.example.uniproject.data.repository.author.AuthorRepository;
import com.example.uniproject.data.repository.book.BookRepository;
import com.example.uniproject.data.repository.category.CategoryRepository;

import java.util.List;

import retrofit2.Call;

public final class BooksViewModel extends ViewModel {
    private final BookRepository bookRepository = new BookRepository();
    private final AuthorRepository authorRepository = new AuthorRepository();
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<BookResponse>> books = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> referenceDataLoading =
            new MutableLiveData<>(false);
    private final MutableLiveData<List<AuthorResponse>> authors = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryResponse>> categories = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> referenceDataError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> creating = new MutableLiveData<>(false);
    private final MutableLiveData<BookResponse> createSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> createError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updating = new MutableLiveData<>(false);
    private final MutableLiveData<BookResponse> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> updateError = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleting = new MutableLiveData<>(false);
    private final MutableLiveData<BookResponse> deleteSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> deleteError = new MutableLiveData<>();

    private String currentQuery = "";
    private int pendingReferenceRequests;
    private Call<List<BookResponse>> activeListCall;
    private Call<List<AuthorResponse>> activeAuthorsCall;
    private Call<List<CategoryResponse>> activeCategoriesCall;
    private Call<BookResponse> activeCreateCall;
    private Call<BookResponse> activeUpdateCall;
    private Call<Void> activeDeleteCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<BookResponse>> getBooks() {
        return books;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public LiveData<Boolean> getReferenceDataLoading() {
        return referenceDataLoading;
    }

    public LiveData<List<AuthorResponse>> getAuthors() {
        return authors;
    }

    public LiveData<List<CategoryResponse>> getCategories() {
        return categories;
    }

    public LiveData<ApiErrorResponse> getReferenceDataError() {
        return referenceDataError;
    }

    public void consumeReferenceDataError() {
        referenceDataError.setValue(null);
    }

    public LiveData<Boolean> getCreating() {
        return creating;
    }

    public LiveData<BookResponse> getCreateSuccess() {
        return createSuccess;
    }

    public LiveData<ApiErrorResponse> getCreateError() {
        return createError;
    }

    public LiveData<Boolean> getUpdating() {
        return updating;
    }

    public LiveData<BookResponse> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<ApiErrorResponse> getUpdateError() {
        return updateError;
    }

    public LiveData<Boolean> getDeleting() {
        return deleting;
    }

    public LiveData<BookResponse> getDeleteSuccess() {
        return deleteSuccess;
    }

    public LiveData<ApiErrorResponse> getDeleteError() {
        return deleteError;
    }

    public String getCurrentQuery() {
        return currentQuery;
    }

    public void loadBooks() {
        searchBooks("");
    }

    public void searchBooks(String query) {
        currentQuery = query == null ? "" : query.trim();

        if (activeListCall != null) {
            activeListCall.cancel();
        }

        loading.setValue(true);
        loadError.setValue(null);

        BookRepository.BookListCallback callback = new BookRepository.BookListCallback() {
            @Override
            public void onSuccess(List<BookResponse> loadedBooks) {
                books.postValue(loadedBooks);
                loading.postValue(false);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                loadError.postValue(error);
                loading.postValue(false);
            }
        };

        if (currentQuery.isEmpty()) {
            activeListCall = bookRepository.getAll(callback);
        } else {
            activeListCall = bookRepository.search(currentQuery, callback);
        }
    }

    public void reloadCurrentList() {
        searchBooks(currentQuery);
    }

    public void loadReferenceData() {
        if (Boolean.TRUE.equals(referenceDataLoading.getValue())) {
            return;
        }
        if (authors.getValue() != null && categories.getValue() != null) {
            return;
        }

        referenceDataLoading.setValue(true);
        referenceDataError.setValue(null);
        pendingReferenceRequests = 2;

        activeAuthorsCall = authorRepository.getAll(
                new AuthorRepository.AuthorListCallback() {
                    @Override
                    public void onSuccess(List<AuthorResponse> loadedAuthors) {
                        authors.postValue(loadedAuthors);
                        finishReferenceRequest(null);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        finishReferenceRequest(error);
                    }
                }
        );

        activeCategoriesCall = categoryRepository.getAll(
                new CategoryRepository.CategoryListCallback() {
                    @Override
                    public void onSuccess(List<CategoryResponse> loadedCategories) {
                        categories.postValue(loadedCategories);
                        finishReferenceRequest(null);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        finishReferenceRequest(error);
                    }
                }
        );
    }

    public void prepareCreate() {
        if (isMutationInProgress()) {
            return;
        }
        clearMutationResults();
    }

    public void createBook(
            String title,
            String isbn,
            Integer publicationYear,
            Integer totalCopies,
            Long authorId,
            Long categoryId
    ) {
        if (isMutationInProgress()) {
            return;
        }

        creating.setValue(true);
        createSuccess.setValue(null);
        createError.setValue(null);

        BookRequest request = new BookRequest(
                title,
                isbn,
                publicationYear,
                totalCopies,
                authorId,
                categoryId
        );
        activeCreateCall = bookRepository.create(
                request,
                new BookRepository.BookMutationCallback() {
                    @Override
                    public void onSuccess(BookResponse book) {
                        creating.postValue(false);
                        createSuccess.postValue(book);
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

    public void updateBook(
            Long id,
            String title,
            String isbn,
            Integer publicationYear,
            Integer totalCopies,
            Long authorId,
            Long categoryId
    ) {
        if (isMutationInProgress()) {
            return;
        }

        updating.setValue(true);
        updateSuccess.setValue(null);
        updateError.setValue(null);

        BookRequest request = new BookRequest(
                title,
                isbn,
                publicationYear,
                totalCopies,
                authorId,
                categoryId
        );
        activeUpdateCall = bookRepository.update(
                id,
                request,
                new BookRepository.BookMutationCallback() {
                    @Override
                    public void onSuccess(BookResponse book) {
                        updating.postValue(false);
                        updateSuccess.postValue(book);
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

    public void deleteBook(BookResponse book) {
        if (isMutationInProgress() || book.getId() == null) {
            return;
        }

        deleting.setValue(true);
        deleteSuccess.setValue(null);
        deleteError.setValue(null);

        activeDeleteCall = bookRepository.delete(
                book.getId(),
                new BookRepository.BookDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        deleting.postValue(false);
                        deleteSuccess.postValue(book);
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

    private void finishReferenceRequest(ApiErrorResponse error) {
        if (error != null) {
            referenceDataError.postValue(error);
        }

        pendingReferenceRequests--;
        if (pendingReferenceRequests <= 0) {
            referenceDataLoading.postValue(false);
        }
    }

    @Override
    protected void onCleared() {
        if (activeListCall != null) {
            activeListCall.cancel();
        }
        if (activeAuthorsCall != null) {
            activeAuthorsCall.cancel();
        }
        if (activeCategoriesCall != null) {
            activeCategoriesCall.cancel();
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
