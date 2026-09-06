package com.example.uniproject.ui.loans;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.model.loan.LoanRequest;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.example.uniproject.data.repository.book.BookRepository;
import com.example.uniproject.data.repository.loan.LoanRepository;
import com.example.uniproject.data.repository.member.MemberRepository;

import java.util.List;

import retrofit2.Call;

public final class LoansViewModel extends ViewModel {
    private final LoanRepository loanRepository = new LoanRepository();
    private final BookRepository bookRepository = new BookRepository();
    private final MemberRepository memberRepository = new MemberRepository();

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<LoanResponse>> loans = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> loadError = new MutableLiveData<>();

    private final MutableLiveData<Boolean> referenceDataLoading =
            new MutableLiveData<>(false);
    private final MutableLiveData<List<BookResponse>> books = new MutableLiveData<>();
    private final MutableLiveData<List<MemberResponse>> members = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> referenceDataError =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> borrowing = new MutableLiveData<>(false);
    private final MutableLiveData<LoanResponse> borrowSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> borrowError = new MutableLiveData<>();

    private final MutableLiveData<Long> returningLoanId = new MutableLiveData<>();
    private final MutableLiveData<LoanResponse> returnSuccess = new MutableLiveData<>();
    private final MutableLiveData<ApiErrorResponse> returnError = new MutableLiveData<>();

    private boolean showingOverdueOnly;
    private int pendingReferenceRequests;

    private Call<List<LoanResponse>> activeListCall;
    private Call<List<BookResponse>> activeBooksCall;
    private Call<List<MemberResponse>> activeMembersCall;
    private Call<LoanResponse> activeBorrowCall;
    private Call<LoanResponse> activeReturnCall;

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<LoanResponse>> getLoans() {
        return loans;
    }

    public LiveData<ApiErrorResponse> getLoadError() {
        return loadError;
    }

    public LiveData<Boolean> getReferenceDataLoading() {
        return referenceDataLoading;
    }

    public LiveData<List<BookResponse>> getBooks() {
        return books;
    }

    public LiveData<List<MemberResponse>> getMembers() {
        return members;
    }

    public LiveData<ApiErrorResponse> getReferenceDataError() {
        return referenceDataError;
    }

    public LiveData<Boolean> getBorrowing() {
        return borrowing;
    }

    public LiveData<LoanResponse> getBorrowSuccess() {
        return borrowSuccess;
    }

    public LiveData<ApiErrorResponse> getBorrowError() {
        return borrowError;
    }

    public LiveData<Long> getReturningLoanId() {
        return returningLoanId;
    }

    public LiveData<LoanResponse> getReturnSuccess() {
        return returnSuccess;
    }

    public LiveData<ApiErrorResponse> getReturnError() {
        return returnError;
    }

    public boolean isShowingOverdueOnly() {
        return showingOverdueOnly;
    }

    public void loadLoans() {
        loadCurrentList();
    }

    public void showAllLoans() {
        showingOverdueOnly = false;
        loadCurrentList();
    }

    public void showOverdueLoans() {
        showingOverdueOnly = true;
        loadCurrentList();
    }

    public void reloadCurrentList() {
        loadCurrentList();
    }

    public void loadReferenceData() {
        if (Boolean.TRUE.equals(referenceDataLoading.getValue())) {
            return;
        }

        referenceDataLoading.setValue(true);
        referenceDataError.setValue(null);
        pendingReferenceRequests = 2;

        activeBooksCall = bookRepository.getAll(new BookRepository.BookListCallback() {
            @Override
            public void onSuccess(List<BookResponse> loadedBooks) {
                books.postValue(loadedBooks);
                finishReferenceRequest(null);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                finishReferenceRequest(error);
            }
        });

        activeMembersCall = memberRepository.getAll(new MemberRepository.MemberListCallback() {
            @Override
            public void onSuccess(List<MemberResponse> loadedMembers) {
                members.postValue(loadedMembers);
                finishReferenceRequest(null);
            }

            @Override
            public void onError(ApiErrorResponse error) {
                finishReferenceRequest(error);
            }
        });
    }

    public void consumeReferenceDataError() {
        referenceDataError.setValue(null);
    }

    public void prepareBorrow() {
        if (isMutationInProgress()) {
            return;
        }
        clearMutationResults();
    }

    public void borrowBook(Long bookId, Long memberId) {
        if (isMutationInProgress() || bookId == null || memberId == null) {
            return;
        }

        borrowing.setValue(true);
        borrowSuccess.setValue(null);
        borrowError.setValue(null);

        LoanRequest request = new LoanRequest(bookId, memberId);
        activeBorrowCall = loanRepository.borrow(
                request,
                new LoanRepository.LoanCallback() {
                    @Override
                    public void onSuccess(LoanResponse loan) {
                        borrowing.postValue(false);
                        borrowSuccess.postValue(loan);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        borrowing.postValue(false);
                        borrowError.postValue(error);
                    }
                }
        );
    }

    public void consumeBorrowSuccess() {
        borrowSuccess.setValue(null);
    }

    public void consumeBorrowError() {
        borrowError.setValue(null);
    }

    public void returnLoan(LoanResponse loan) {
        if (isMutationInProgress()
                || loan == null
                || loan.getId() == null
                || "RETURNED".equalsIgnoreCase(loan.getStatus())) {
            return;
        }

        returningLoanId.setValue(loan.getId());
        returnSuccess.setValue(null);
        returnError.setValue(null);

        activeReturnCall = loanRepository.returnLoan(
                loan.getId(),
                new LoanRepository.LoanCallback() {
                    @Override
                    public void onSuccess(LoanResponse returnedLoan) {
                        returningLoanId.postValue(null);
                        returnSuccess.postValue(returnedLoan);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        returningLoanId.postValue(null);
                        returnError.postValue(error);
                    }
                }
        );
    }

    public void consumeReturnSuccess() {
        returnSuccess.setValue(null);
    }

    public void consumeReturnError() {
        returnError.setValue(null);
    }

    private void loadCurrentList() {
        if (activeListCall != null) {
            activeListCall.cancel();
        }

        loading.setValue(true);
        loadError.setValue(null);

        LoanRepository.LoanListCallback callback =
                new LoanRepository.LoanListCallback() {
                    @Override
                    public void onSuccess(List<LoanResponse> loadedLoans) {
                        loans.postValue(loadedLoans);
                        loading.postValue(false);
                    }

                    @Override
                    public void onError(ApiErrorResponse error) {
                        loadError.postValue(error);
                        loading.postValue(false);
                    }
                };

        activeListCall = showingOverdueOnly
                ? loanRepository.getOverdue(callback)
                : loanRepository.getAll(callback);
    }

    private boolean isMutationInProgress() {
        return Boolean.TRUE.equals(borrowing.getValue())
                || returningLoanId.getValue() != null;
    }

    private void clearMutationResults() {
        borrowSuccess.setValue(null);
        borrowError.setValue(null);
        returnSuccess.setValue(null);
        returnError.setValue(null);
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
        if (activeBooksCall != null) {
            activeBooksCall.cancel();
        }
        if (activeMembersCall != null) {
            activeMembersCall.cancel();
        }
        if (activeBorrowCall != null) {
            activeBorrowCall.cancel();
        }
        if (activeReturnCall != null) {
            activeReturnCall.cancel();
        }
        super.onCleared();
    }
}
