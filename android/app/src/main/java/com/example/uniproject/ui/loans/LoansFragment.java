package com.example.uniproject.ui.loans;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uniproject.R;
import com.example.uniproject.auth.SessionManager;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.model.loan.LoanResponse;
import com.example.uniproject.data.model.member.MemberResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class LoansFragment extends Fragment {
    public static final String ARG_SHOW_LOGIN_SUCCESS = "showLoginSuccess";

    private boolean loginConfirmationShown;
    private LoansViewModel viewModel;
    private LoanAdapter adapter;
    private MaterialButtonToggleGroup loanFilterToggle;
    private MaterialButton allLoansFilterButton;
    private MaterialButton overdueLoansFilterButton;
    private RecyclerView loansRecyclerView;
    private ProgressBar loansProgress;
    private TextView loansEmptyText;
    private View loansErrorContainer;
    private TextView loansErrorText;
    private MaterialButton loansRetryButton;
    private FloatingActionButton addLoanButton;

    private List<BookResponse> availableBooks = Collections.emptyList();
    private List<MemberResponse> availableMembers = Collections.emptyList();
    private List<BookResponse> borrowBookChoices = Collections.emptyList();
    private List<MemberResponse> borrowMemberChoices = Collections.emptyList();

    private AlertDialog loanFormDialog;
    private TextInputLayout loanBookInputLayout;
    private MaterialAutoCompleteTextView loanBookInput;
    private TextInputLayout loanMemberInputLayout;
    private MaterialAutoCompleteTextView loanMemberInput;
    private TextView loanFormErrorText;
    private ProgressBar loanFormProgress;
    private Long selectedBookId;
    private Long selectedMemberId;

    public LoansFragment() {
        super(R.layout.fragment_loans);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoansViewModel.class);
        bindViews(view);
        configureList();
        configureFilters();
        observeViewModel();
        showLoginSuccessIfNeeded(view);

        loansRetryButton.setOnClickListener(ignored -> viewModel.reloadCurrentList());
        addLoanButton.setOnClickListener(ignored -> showBorrowDialog());

        if (viewModel.getLoans().getValue() == null
                && viewModel.getLoadError().getValue() == null) {
            viewModel.loadLoans();
        }
        viewModel.loadReferenceData();
        updateLoanActionState();
    }

    private void showLoginSuccessIfNeeded(View view) {
        Bundle arguments = getArguments();
        boolean shouldShowLoginSuccess = arguments != null
                && arguments.getBoolean(ARG_SHOW_LOGIN_SUCCESS, false);
        if (!shouldShowLoginSuccess || loginConfirmationShown) {
            return;
        }

        SessionManager sessionManager = new SessionManager(requireContext());
        String displayName = sessionManager.getFullName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = sessionManager.getUsername();
        }
        if (TextUtils.isEmpty(displayName)) {
            return;
        }

        loginConfirmationShown = true;
        Snackbar.make(
                view,
                getString(R.string.login_success, displayName),
                Snackbar.LENGTH_LONG
        ).show();
    }

    private void bindViews(View view) {
        loanFilterToggle = view.findViewById(R.id.loanFilterToggle);
        allLoansFilterButton = view.findViewById(R.id.allLoansFilterButton);
        overdueLoansFilterButton = view.findViewById(R.id.overdueLoansFilterButton);
        loansRecyclerView = view.findViewById(R.id.loansRecyclerView);
        loansProgress = view.findViewById(R.id.loansProgress);
        loansEmptyText = view.findViewById(R.id.loansEmptyText);
        loansErrorContainer = view.findViewById(R.id.loansErrorContainer);
        loansErrorText = view.findViewById(R.id.loansErrorText);
        loansRetryButton = view.findViewById(R.id.loansRetryButton);
        addLoanButton = view.findViewById(R.id.addLoanButton);
    }

    private void configureList() {
        adapter = new LoanAdapter(this::showReturnConfirmation);
        loansRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        loansRecyclerView.setAdapter(adapter);
    }

    private void configureFilters() {
        loanFilterToggle.check(viewModel.isShowingOverdueOnly()
                ? R.id.overdueLoansFilterButton
                : R.id.allLoansFilterButton);
        loanFilterToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }

            if (checkedId == R.id.allLoansFilterButton
                    && viewModel.isShowingOverdueOnly()) {
                viewModel.showAllLoans();
            } else if (checkedId == R.id.overdueLoansFilterButton
                    && !viewModel.isShowingOverdueOnly()) {
                viewModel.showOverdueLoans();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getLoans().observe(getViewLifecycleOwner(), loadedLoans -> {
            if (loadedLoans != null) {
                renderLoans(loadedLoans);
            }
        });
        viewModel.getLoadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderLoadError(error);
            }
        });
        viewModel.getBooks().observe(getViewLifecycleOwner(), loadedBooks -> {
            if (loadedBooks != null) {
                availableBooks = new ArrayList<>(loadedBooks);
                updateLoanActionState();
            }
        });
        viewModel.getMembers().observe(getViewLifecycleOwner(), loadedMembers -> {
            if (loadedMembers != null) {
                availableMembers = new ArrayList<>(loadedMembers);
                adapter.submitMembers(loadedMembers);
                updateLoanActionState();
            }
        });
        viewModel.getReferenceDataLoading().observe(
                getViewLifecycleOwner(),
                ignored -> updateLoanActionState()
        );
        viewModel.getReferenceDataError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderReferenceDataError(error);
            }
        });
        viewModel.getBorrowing().observe(getViewLifecycleOwner(), this::renderBorrowing);
        viewModel.getBorrowError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderBorrowError(error);
            }
        });
        viewModel.getBorrowSuccess().observe(getViewLifecycleOwner(), loan -> {
            if (loan != null) {
                renderBorrowSuccess(loan);
            }
        });
        viewModel.getReturningLoanId().observe(
                getViewLifecycleOwner(),
                ignored -> updateLoanActionState()
        );
        viewModel.getReturnError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderReturnError(error);
            }
        });
        viewModel.getReturnSuccess().observe(getViewLifecycleOwner(), loan -> {
            if (loan != null) {
                renderReturnSuccess(loan);
            }
        });
    }

    private void showBorrowDialog() {
        if (loanFormDialog != null && loanFormDialog.isShowing()) {
            return;
        }
        if (!referenceDataReady()) {
            Snackbar.make(requireView(), R.string.loan_reference_data_failed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_action, ignored -> viewModel.loadReferenceData())
                    .show();
            return;
        }

        borrowBookChoices = availableBookChoices();
        if (borrowBookChoices.isEmpty()) {
            Snackbar.make(requireView(), R.string.loan_no_available_books, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        borrowMemberChoices = activeMemberChoices();
        if (borrowMemberChoices.isEmpty()) {
            Snackbar.make(requireView(), R.string.loan_no_active_members, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        viewModel.prepareBorrow();
        selectedBookId = null;
        selectedMemberId = null;

        View formView = getLayoutInflater().inflate(R.layout.dialog_loan_form, null, false);
        bindLoanForm(formView);
        configureLoanDropdowns();

        loanFormDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.create_loan_title)
                .setView(formView)
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.loan_borrow_action, null)
                .create();

        loanFormDialog.setOnShowListener(ignored -> {
            Button borrowButton = loanFormDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            borrowButton.setOnClickListener(button -> attemptBorrow());
            renderBorrowing(null);
            loanBookInput.requestFocus();
        });
        loanFormDialog.setOnDismissListener(ignored -> clearLoanFormReferences());
        loanFormDialog.show();
    }

    private void bindLoanForm(View formView) {
        loanBookInputLayout = formView.findViewById(R.id.loanBookInputLayout);
        loanBookInput = formView.findViewById(R.id.loanBookInput);
        loanMemberInputLayout = formView.findViewById(R.id.loanMemberInputLayout);
        loanMemberInput = formView.findViewById(R.id.loanMemberInput);
        loanFormErrorText = formView.findViewById(R.id.loanFormErrorText);
        loanFormProgress = formView.findViewById(R.id.loanFormProgress);
    }

    private void configureLoanDropdowns() {
        List<String> bookOptions = new ArrayList<>();
        for (BookResponse book : borrowBookChoices) {
            bookOptions.add(bookOption(book));
        }
        loanBookInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                bookOptions
        ));
        loanBookInput.setOnItemClickListener((parent, view, position, id) -> {
            selectedBookId = borrowBookChoices.get(position).getId();
            loanBookInputLayout.setError(null);
        });

        List<String> memberOptions = new ArrayList<>();
        for (MemberResponse member : borrowMemberChoices) {
            memberOptions.add(memberOption(member));
        }
        loanMemberInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                memberOptions
        ));
        loanMemberInput.setOnItemClickListener((parent, view, position, id) -> {
            selectedMemberId = borrowMemberChoices.get(position).getId();
            loanMemberInputLayout.setError(null);
        });
    }

    private void attemptBorrow() {
        clearLoanFormErrors();
        if (selectedBookId == null) {
            loanBookInputLayout.setError(getString(R.string.loan_book_required));
            loanBookInput.requestFocus();
            return;
        }
        if (selectedMemberId == null) {
            loanMemberInputLayout.setError(getString(R.string.loan_member_required));
            loanMemberInput.requestFocus();
            return;
        }

        viewModel.borrowBook(selectedBookId, selectedMemberId);
    }

    private void showReturnConfirmation(LoanResponse loan) {
        if (loan == null || loan.getId() == null) {
            return;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.return_loan_title)
                .setMessage(getString(R.string.return_loan_confirmation, loanBookTitle(loan)))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(
                        R.string.return_loan_action,
                        (dialog, which) -> viewModel.returnLoan(loan)
                )
                .show();
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        loansProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            loansRecyclerView.setVisibility(View.GONE);
            loansEmptyText.setVisibility(View.GONE);
            loansErrorContainer.setVisibility(View.GONE);
        }
        updateLoanActionState();
    }

    private void renderLoans(List<LoanResponse> loadedLoans) {
        adapter.submitList(loadedLoans);
        loansProgress.setVisibility(View.GONE);
        loansErrorContainer.setVisibility(View.GONE);

        boolean empty = loadedLoans.isEmpty();
        loansEmptyText.setText(viewModel.isShowingOverdueOnly()
                ? R.string.loans_overdue_empty
                : R.string.loans_empty);
        loansEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        loansRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderLoadError(ApiErrorResponse error) {
        loansProgress.setVisibility(View.GONE);
        loansRecyclerView.setVisibility(View.GONE);
        loansEmptyText.setVisibility(View.GONE);
        loansErrorContainer.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        loansErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.loans_load_failed)
                : message);
    }

    private void renderReferenceDataError(ApiErrorResponse error) {
        viewModel.consumeReferenceDataError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.loan_reference_data_failed)
                : message;
        Snackbar.make(requireView(), displayMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry_action, ignored -> viewModel.loadReferenceData())
                .show();
        updateLoanActionState();
    }

    private void renderBorrowing(Boolean ignored) {
        updateLoanActionState();
        if (loanFormDialog == null || !loanFormDialog.isShowing()) {
            return;
        }

        boolean borrowing = Boolean.TRUE.equals(viewModel.getBorrowing().getValue());
        loanBookInput.setEnabled(!borrowing);
        loanMemberInput.setEnabled(!borrowing);
        loanFormProgress.setVisibility(borrowing ? View.VISIBLE : View.GONE);
        loanFormDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!borrowing);
        loanFormDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!borrowing);
        loanFormDialog.setCanceledOnTouchOutside(!borrowing);
    }

    private void renderBorrowError(ApiErrorResponse error) {
        viewModel.consumeBorrowError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.loan_borrow_failed)
                : message;

        if (loanFormDialog == null || !loanFormDialog.isShowing()) {
            Snackbar.make(requireView(), displayMessage, Snackbar.LENGTH_LONG).show();
            return;
        }

        clearLoanFormErrors();
        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            if (!TextUtils.isEmpty(fieldErrors.get("bookId"))) {
                loanBookInputLayout.setError(getString(R.string.loan_book_required));
            }
            if (!TextUtils.isEmpty(fieldErrors.get("memberId"))) {
                loanMemberInputLayout.setError(getString(R.string.loan_member_required));
            }
        }
        loanFormErrorText.setText(displayMessage);
        loanFormErrorText.setVisibility(View.VISIBLE);
    }

    private void renderBorrowSuccess(LoanResponse loan) {
        viewModel.consumeBorrowSuccess();
        if (loanFormDialog != null) {
            loanFormDialog.dismiss();
        }
        Snackbar.make(
                requireView(),
                getString(R.string.loan_borrow_success, loanBookTitle(loan)),
                Snackbar.LENGTH_LONG
        ).show();
        refreshAfterMutation();
    }

    private void renderReturnError(ApiErrorResponse error) {
        viewModel.consumeReturnError();
        String message = error.getMessage();
        Snackbar.make(
                requireView(),
                TextUtils.isEmpty(message) ? getString(R.string.loan_return_failed) : message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private void renderReturnSuccess(LoanResponse loan) {
        viewModel.consumeReturnSuccess();
        Snackbar.make(
                requireView(),
                getString(R.string.loan_return_success, loanBookTitle(loan)),
                Snackbar.LENGTH_LONG
        ).show();
        refreshAfterMutation();
    }

    private void refreshAfterMutation() {
        viewModel.reloadCurrentList();
        viewModel.loadReferenceData();
    }

    private void updateLoanActionState() {
        if (viewModel == null) {
            return;
        }

        boolean mutationBusy = Boolean.TRUE.equals(viewModel.getBorrowing().getValue())
                || viewModel.getReturningLoanId().getValue() != null;
        boolean referenceDataLoading = Boolean.TRUE.equals(
                viewModel.getReferenceDataLoading().getValue()
        );
        boolean listLoading = Boolean.TRUE.equals(viewModel.getLoading().getValue());

        if (addLoanButton != null) {
            addLoanButton.setEnabled(
                    !mutationBusy && !referenceDataLoading && referenceDataReady()
            );
        }
        if (allLoansFilterButton != null) {
            allLoansFilterButton.setEnabled(!mutationBusy);
        }
        if (overdueLoansFilterButton != null) {
            overdueLoansFilterButton.setEnabled(!mutationBusy);
        }
        if (loansRetryButton != null) {
            loansRetryButton.setEnabled(!listLoading);
        }
        if (adapter != null) {
            adapter.setReturningLoanId(viewModel.getReturningLoanId().getValue());
            adapter.setActionsEnabled(!mutationBusy);
        }
    }

    private boolean referenceDataReady() {
        return viewModel != null
                && viewModel.getBooks().getValue() != null
                && viewModel.getMembers().getValue() != null;
    }

    private List<BookResponse> availableBookChoices() {
        List<BookResponse> choices = new ArrayList<>();
        for (BookResponse book : availableBooks) {
            if (book.getId() != null
                    && book.getAvailableCopies() != null
                    && book.getAvailableCopies() > 0) {
                choices.add(book);
            }
        }
        return choices;
    }

    private List<MemberResponse> activeMemberChoices() {
        List<MemberResponse> choices = new ArrayList<>();
        for (MemberResponse member : availableMembers) {
            if (member.getId() != null && Boolean.TRUE.equals(member.getActive())) {
                choices.add(member);
            }
        }
        return choices;
    }

    private String bookOption(BookResponse book) {
        String title = TextUtils.isEmpty(book.getTitle())
                ? getString(R.string.loan_book_missing)
                : book.getTitle();
        int availableCopies = book.getAvailableCopies() == null
                ? 0
                : book.getAvailableCopies();
        return getString(R.string.loan_book_option, title, availableCopies);
    }

    private String memberOption(MemberResponse member) {
        String cardNumber = TextUtils.isEmpty(member.getMembershipCardNumber())
                ? getString(R.string.member_value_missing)
                : member.getMembershipCardNumber();
        return getString(R.string.loan_member_option, memberName(member), cardNumber);
    }

    private String memberName(MemberResponse member) {
        String firstName = member.getFirstName() == null ? "" : member.getFirstName();
        String lastName = member.getLastName() == null ? "" : member.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty()
                ? getString(R.string.member_name_missing)
                : fullName;
    }

    private String loanBookTitle(LoanResponse loan) {
        return TextUtils.isEmpty(loan.getBookTitleAtLoan())
                ? getString(R.string.loan_book_missing)
                : loan.getBookTitleAtLoan();
    }

    private void clearLoanFormErrors() {
        loanBookInputLayout.setError(null);
        loanMemberInputLayout.setError(null);
        loanFormErrorText.setText(null);
        loanFormErrorText.setVisibility(View.GONE);
    }

    private void clearLoanFormReferences() {
        loanFormDialog = null;
        loanBookInputLayout = null;
        loanBookInput = null;
        loanMemberInputLayout = null;
        loanMemberInput = null;
        loanFormErrorText = null;
        loanFormProgress = null;
        borrowBookChoices = Collections.emptyList();
        borrowMemberChoices = Collections.emptyList();
        selectedBookId = null;
        selectedMemberId = null;
    }

    @Override
    public void onDestroyView() {
        if (loanFormDialog != null) {
            loanFormDialog.dismiss();
        }
        loansRecyclerView.setAdapter(null);
        adapter = null;
        loanFilterToggle = null;
        allLoansFilterButton = null;
        overdueLoansFilterButton = null;
        loansRecyclerView = null;
        loansProgress = null;
        loansEmptyText = null;
        loansErrorContainer = null;
        loansErrorText = null;
        loansRetryButton = null;
        addLoanButton = null;
        availableBooks = Collections.emptyList();
        availableMembers = Collections.emptyList();
        super.onDestroyView();
    }
}
