package com.example.uniproject.ui.books;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.author.AuthorResponse;
import com.example.uniproject.data.model.book.BookResponse;
import com.example.uniproject.data.model.category.CategoryResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class BooksFragment extends Fragment {
    private static final int BOOK_TITLE_MAX_LENGTH = 255;
    private static final int BOOK_ISBN_MAX_LENGTH = 20;
    private static final long SEARCH_DEBOUNCE_MILLIS = 350L;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());

    private BooksViewModel viewModel;
    private BookAdapter adapter;
    private TextInputLayout bookSearchInputLayout;
    private TextInputEditText bookSearchInput;
    private RecyclerView booksRecyclerView;
    private ProgressBar booksProgress;
    private TextView booksEmptyText;
    private View booksErrorContainer;
    private TextView booksErrorText;
    private MaterialButton booksRetryButton;
    private FloatingActionButton addBookButton;

    private List<AuthorResponse> availableAuthors = Collections.emptyList();
    private List<CategoryResponse> availableCategories = Collections.emptyList();
    private AlertDialog bookFormDialog;
    private TextInputLayout bookTitleInputLayout;
    private TextInputEditText bookTitleInput;
    private TextInputLayout bookIsbnInputLayout;
    private TextInputEditText bookIsbnInput;
    private TextInputLayout bookPublicationYearInputLayout;
    private TextInputEditText bookPublicationYearInput;
    private TextInputLayout bookTotalCopiesInputLayout;
    private TextInputEditText bookTotalCopiesInput;
    private TextInputLayout bookAuthorInputLayout;
    private MaterialAutoCompleteTextView bookAuthorInput;
    private TextInputLayout bookCategoryInputLayout;
    private MaterialAutoCompleteTextView bookCategoryInput;
    private TextView bookFormErrorText;
    private ProgressBar bookFormProgress;
    private BookResponse editingBook;
    private Long selectedAuthorId;
    private Long selectedCategoryId;
    private TextWatcher searchTextWatcher;
    private Runnable pendingSearch;

    public BooksFragment() {
        super(R.layout.fragment_books);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BooksViewModel.class);
        bindViews(view);
        configureList();
        bookSearchInput.setText(viewModel.getCurrentQuery());
        bookSearchInput.setSelection(bookSearchInput.length());
        configureSearch();
        observeViewModel();

        booksRetryButton.setOnClickListener(ignored -> viewModel.reloadCurrentList());
        addBookButton.setOnClickListener(ignored -> showCreateBookDialog());

        if (viewModel.getBooks().getValue() == null
                && viewModel.getLoadError().getValue() == null) {
            viewModel.reloadCurrentList();
        }
        viewModel.loadReferenceData();
        updateBookActionState();
    }

    private void bindViews(View view) {
        bookSearchInputLayout = view.findViewById(R.id.bookSearchInputLayout);
        bookSearchInput = view.findViewById(R.id.bookSearchInput);
        booksRecyclerView = view.findViewById(R.id.booksRecyclerView);
        booksProgress = view.findViewById(R.id.booksProgress);
        booksEmptyText = view.findViewById(R.id.booksEmptyText);
        booksErrorContainer = view.findViewById(R.id.booksErrorContainer);
        booksErrorText = view.findViewById(R.id.booksErrorText);
        booksRetryButton = view.findViewById(R.id.booksRetryButton);
        addBookButton = view.findViewById(R.id.addBookButton);
    }

    private void configureList() {
        adapter = new BookAdapter(
                this::showEditBookDialog,
                this::showDeleteBookConfirmation
        );
        booksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        booksRecyclerView.setAdapter(adapter);
    }

    private void configureSearch() {
        bookSearchInputLayout.setEndIconOnClickListener(ignored -> performSearch());
        bookSearchInput.setOnEditorActionListener((input, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                return false;
            }

            performSearch();
            return true;
        });

        searchTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                scheduleLiveSearch(editable == null ? "" : editable.toString());
            }
        };
        bookSearchInput.addTextChangedListener(searchTextWatcher);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getBooks().observe(getViewLifecycleOwner(), loadedBooks -> {
            if (loadedBooks != null) {
                renderBooks(loadedBooks);
            }
        });
        viewModel.getLoadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderError(error);
            }
        });
        viewModel.getAuthors().observe(getViewLifecycleOwner(), loadedAuthors -> {
            if (loadedAuthors != null) {
                availableAuthors = new ArrayList<>(loadedAuthors);
                adapter.submitAuthors(loadedAuthors);
                updateBookActionState();
            }
        });
        viewModel.getCategories().observe(getViewLifecycleOwner(), loadedCategories -> {
            if (loadedCategories != null) {
                availableCategories = new ArrayList<>(loadedCategories);
                adapter.submitCategories(loadedCategories);
                updateBookActionState();
            }
        });
        viewModel.getReferenceDataLoading().observe(
                getViewLifecycleOwner(),
                ignored -> updateBookActionState()
        );
        viewModel.getReferenceDataError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderReferenceDataError(error);
            }
        });
        viewModel.getCreating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getCreateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderBookFormError(error);
            }
        });
        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), book -> {
            if (book != null) {
                renderCreateSuccess(book);
            }
        });
        viewModel.getUpdating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getUpdateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderBookFormError(error);
            }
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), book -> {
            if (book != null) {
                renderUpdateSuccess(book);
            }
        });
        viewModel.getDeleting().observe(getViewLifecycleOwner(), this::renderDeleting);
        viewModel.getDeleteError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderDeleteError(error);
            }
        });
        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), book -> {
            if (book != null) {
                renderDeleteSuccess(book);
            }
        });
    }

    private void performSearch() {
        cancelPendingSearch();
        hideKeyboard();
        viewModel.searchBooks(textOf(bookSearchInput));
    }

    private void scheduleLiveSearch(String input) {
        cancelPendingSearch();

        String query = input == null ? "" : input.trim();
        if (query.isEmpty()) {
            if (!viewModel.getCurrentQuery().isEmpty()) {
                viewModel.loadBooks();
            }
            return;
        }
        if (query.equals(viewModel.getCurrentQuery())) {
            return;
        }

        pendingSearch = () -> {
            if (bookSearchInput == null) {
                return;
            }

            String latestQuery = textOf(bookSearchInput).trim();
            if (latestQuery.equals(query)) {
                viewModel.searchBooks(query);
            }
        };
        searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MILLIS);
    }

    private void cancelPendingSearch() {
        if (pendingSearch != null) {
            searchHandler.removeCallbacks(pendingSearch);
            pendingSearch = null;
        }
    }

    private void showCreateBookDialog() {
        showBookFormDialog(null);
    }

    private void showEditBookDialog(BookResponse book) {
        showBookFormDialog(book);
    }

    private void showDeleteBookConfirmation(BookResponse book) {
        String title = bookTitle(book);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_book_title)
                .setMessage(getString(R.string.delete_book_confirmation, title))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(
                        R.string.delete_book_action,
                        (dialog, which) -> viewModel.deleteBook(book)
                )
                .show();
    }

    private void showBookFormDialog(@Nullable BookResponse book) {
        if (bookFormDialog != null && bookFormDialog.isShowing()) {
            return;
        }
        if (!referenceDataReady()) {
            Snackbar.make(
                    requireView(),
                    R.string.book_reference_data_failed,
                    Snackbar.LENGTH_LONG
            ).setAction(
                    R.string.retry_action,
                    ignored -> viewModel.loadReferenceData()
            ).show();
            return;
        }
        if (book != null && book.getId() == null) {
            return;
        }

        editingBook = book;
        if (editingBook == null) {
            viewModel.prepareCreate();
        } else {
            viewModel.prepareUpdate();
        }

        View formView = getLayoutInflater().inflate(R.layout.dialog_book_form, null, false);
        bindBookForm(formView);
        configureBookDropdowns();
        populateBookForm(editingBook);

        bookFormDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingBook == null
                        ? R.string.create_book_title
                        : R.string.edit_book_title)
                .setView(formView)
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.book_save_action, null)
                .create();

        bookFormDialog.setOnShowListener(ignored -> {
            Button saveButton = bookFormDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(button -> attemptSaveBook());
            renderSaving(null);
            bookTitleInput.requestFocus();
            bookTitleInput.setSelection(bookTitleInput.length());
        });
        bookFormDialog.setOnDismissListener(ignored -> clearBookFormReferences());
        bookFormDialog.show();
    }

    private void bindBookForm(View formView) {
        bookTitleInputLayout = formView.findViewById(R.id.bookTitleInputLayout);
        bookTitleInput = formView.findViewById(R.id.bookTitleInput);
        bookIsbnInputLayout = formView.findViewById(R.id.bookIsbnInputLayout);
        bookIsbnInput = formView.findViewById(R.id.bookIsbnInput);
        bookPublicationYearInputLayout = formView.findViewById(
                R.id.bookPublicationYearInputLayout
        );
        bookPublicationYearInput = formView.findViewById(R.id.bookPublicationYearInput);
        bookTotalCopiesInputLayout = formView.findViewById(R.id.bookTotalCopiesInputLayout);
        bookTotalCopiesInput = formView.findViewById(R.id.bookTotalCopiesInput);
        bookAuthorInputLayout = formView.findViewById(R.id.bookAuthorInputLayout);
        bookAuthorInput = formView.findViewById(R.id.bookAuthorInput);
        bookCategoryInputLayout = formView.findViewById(R.id.bookCategoryInputLayout);
        bookCategoryInput = formView.findViewById(R.id.bookCategoryInput);
        bookFormErrorText = formView.findViewById(R.id.bookFormErrorText);
        bookFormProgress = formView.findViewById(R.id.bookFormProgress);
    }

    private void configureBookDropdowns() {
        List<String> authorNames = new ArrayList<>();
        for (AuthorResponse author : availableAuthors) {
            authorNames.add(authorName(author));
        }
        bookAuthorInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                authorNames
        ));
        bookAuthorInput.setOnItemClickListener((parent, view, position, id) -> {
            selectedAuthorId = availableAuthors.get(position).getId();
            bookAuthorInputLayout.setError(null);
        });

        List<String> categoryNames = new ArrayList<>();
        for (CategoryResponse category : availableCategories) {
            categoryNames.add(categoryName(category));
        }
        bookCategoryInput.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categoryNames
        ));
        bookCategoryInput.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategoryId = availableCategories.get(position).getId();
            bookCategoryInputLayout.setError(null);
        });
    }

    private void populateBookForm(@Nullable BookResponse book) {
        selectedAuthorId = null;
        selectedCategoryId = null;
        if (book == null) {
            return;
        }

        bookTitleInput.setText(book.getTitle());
        bookIsbnInput.setText(book.getIsbn());
        if (book.getPublicationYear() != null) {
            bookPublicationYearInput.setText(String.valueOf(book.getPublicationYear()));
        }
        if (book.getTotalCopies() != null) {
            bookTotalCopiesInput.setText(String.valueOf(book.getTotalCopies()));
        }

        int authorPosition = findAuthorPosition(book.getAuthorId());
        if (authorPosition >= 0) {
            selectedAuthorId = availableAuthors.get(authorPosition).getId();
            bookAuthorInput.setText(authorName(availableAuthors.get(authorPosition)), false);
        }

        int categoryPosition = findCategoryPosition(book.getCategoryId());
        if (categoryPosition >= 0) {
            selectedCategoryId = availableCategories.get(categoryPosition).getId();
            bookCategoryInput.setText(categoryName(availableCategories.get(categoryPosition)), false);
        }
    }

    private int findAuthorPosition(Long authorId) {
        for (int index = 0; index < availableAuthors.size(); index++) {
            if (authorId != null && authorId.equals(availableAuthors.get(index).getId())) {
                return index;
            }
        }
        return -1;
    }

    private int findCategoryPosition(Long categoryId) {
        for (int index = 0; index < availableCategories.size(); index++) {
            if (categoryId != null && categoryId.equals(availableCategories.get(index).getId())) {
                return index;
            }
        }
        return -1;
    }

    private void attemptSaveBook() {
        clearBookFormErrors();

        String title = textOf(bookTitleInput).trim();
        String isbn = textOf(bookIsbnInput).trim();
        String publicationYearText = textOf(bookPublicationYearInput).trim();
        String totalCopiesText = textOf(bookTotalCopiesInput).trim();

        if (TextUtils.isEmpty(title)) {
            bookTitleInputLayout.setError(getString(R.string.book_title_required));
            bookTitleInput.requestFocus();
            return;
        }
        if (title.length() > BOOK_TITLE_MAX_LENGTH) {
            bookTitleInputLayout.setError(getString(R.string.book_title_length));
            bookTitleInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(isbn)) {
            bookIsbnInputLayout.setError(getString(R.string.book_isbn_required));
            bookIsbnInput.requestFocus();
            return;
        }
        if (isbn.length() > BOOK_ISBN_MAX_LENGTH) {
            bookIsbnInputLayout.setError(getString(R.string.book_isbn_length));
            bookIsbnInput.requestFocus();
            return;
        }

        Integer publicationYear = parseRequiredInteger(
                publicationYearText,
                bookPublicationYearInputLayout,
                bookPublicationYearInput,
                R.string.book_publication_year_required,
                R.string.book_publication_year_invalid
        );
        if (publicationYear == null) {
            return;
        }

        Integer totalCopies = parseRequiredInteger(
                totalCopiesText,
                bookTotalCopiesInputLayout,
                bookTotalCopiesInput,
                R.string.book_total_copies_required,
                R.string.book_total_copies_invalid
        );
        if (totalCopies == null) {
            return;
        }
        if (totalCopies < 0) {
            bookTotalCopiesInputLayout.setError(getString(
                    R.string.book_total_copies_minimum
            ));
            bookTotalCopiesInput.requestFocus();
            return;
        }
        if (selectedAuthorId == null) {
            bookAuthorInputLayout.setError(getString(R.string.book_author_required));
            bookAuthorInput.requestFocus();
            return;
        }
        if (selectedCategoryId == null) {
            bookCategoryInputLayout.setError(getString(R.string.book_category_required));
            bookCategoryInput.requestFocus();
            return;
        }

        hideKeyboard();
        if (editingBook == null) {
            viewModel.createBook(
                    title,
                    isbn,
                    publicationYear,
                    totalCopies,
                    selectedAuthorId,
                    selectedCategoryId
            );
        } else {
            viewModel.updateBook(
                    editingBook.getId(),
                    title,
                    isbn,
                    publicationYear,
                    totalCopies,
                    selectedAuthorId,
                    selectedCategoryId
            );
        }
    }

    @Nullable
    private Integer parseRequiredInteger(
            String value,
            TextInputLayout inputLayout,
            TextInputEditText input,
            int requiredMessage,
            int invalidMessage
    ) {
        if (TextUtils.isEmpty(value)) {
            inputLayout.setError(getString(requiredMessage));
            input.requestFocus();
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            inputLayout.setError(getString(invalidMessage));
            input.requestFocus();
            return null;
        }
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        booksProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            booksRecyclerView.setVisibility(View.GONE);
            booksEmptyText.setVisibility(View.GONE);
            booksErrorContainer.setVisibility(View.GONE);
        }
        updateBookActionState();
    }

    private void renderBooks(List<BookResponse> loadedBooks) {
        adapter.submitList(loadedBooks);
        booksProgress.setVisibility(View.GONE);
        booksErrorContainer.setVisibility(View.GONE);

        boolean empty = loadedBooks.isEmpty();
        booksEmptyText.setText(viewModel.getCurrentQuery().isEmpty()
                ? R.string.books_empty
                : R.string.books_search_empty);
        booksEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        booksRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderError(ApiErrorResponse error) {
        booksProgress.setVisibility(View.GONE);
        booksRecyclerView.setVisibility(View.GONE);
        booksEmptyText.setVisibility(View.GONE);
        booksErrorContainer.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        booksErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.books_load_failed)
                : message);
    }

    private void renderReferenceDataError(ApiErrorResponse error) {
        viewModel.consumeReferenceDataError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.book_reference_data_failed)
                : message;
        Snackbar.make(requireView(), displayMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(
                        R.string.retry_action,
                        ignored -> viewModel.loadReferenceData()
                )
                .show();
        updateBookActionState();
    }

    private void renderSaving(Boolean ignored) {
        updateBookActionState();
        if (bookFormDialog == null || !bookFormDialog.isShowing()) {
            return;
        }

        boolean saving = Boolean.TRUE.equals(viewModel.getCreating().getValue())
                || Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        bookTitleInput.setEnabled(!saving);
        bookIsbnInput.setEnabled(!saving);
        bookPublicationYearInput.setEnabled(!saving);
        bookTotalCopiesInput.setEnabled(!saving);
        bookAuthorInput.setEnabled(!saving);
        bookCategoryInput.setEnabled(!saving);
        bookFormProgress.setVisibility(saving ? View.VISIBLE : View.GONE);
        bookFormDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!saving);
        bookFormDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!saving);
        bookFormDialog.setCanceledOnTouchOutside(!saving);
    }

    private void renderBookFormError(ApiErrorResponse error) {
        if (bookFormDialog == null || !bookFormDialog.isShowing()) {
            return;
        }

        clearBookFormErrors();
        if (error.getStatus() == 409) {
            bookIsbnInputLayout.setError(getString(R.string.book_isbn_duplicate));
            bookIsbnInput.requestFocus();
            return;
        }

        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            setBookFieldErrors(fieldErrors);
        }

        String message = error.getMessage();
        bookFormErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.book_save_failed)
                : message);
        bookFormErrorText.setVisibility(View.VISIBLE);
    }

    private void setBookFieldErrors(Map<String, String> fieldErrors) {
        String titleError = fieldErrors.get("title");
        if (!TextUtils.isEmpty(titleError)) {
            bookTitleInputLayout.setError(getString(isRequiredError(titleError)
                    ? R.string.book_title_required
                    : R.string.book_title_length));
        }

        String isbnError = fieldErrors.get("isbn");
        if (!TextUtils.isEmpty(isbnError)) {
            bookIsbnInputLayout.setError(getString(isRequiredError(isbnError)
                    ? R.string.book_isbn_required
                    : R.string.book_isbn_length));
        }

        if (!TextUtils.isEmpty(fieldErrors.get("publicationYear"))) {
            bookPublicationYearInputLayout.setError(getString(
                    R.string.book_publication_year_required
            ));
        }

        String totalCopiesError = fieldErrors.get("totalCopies");
        if (!TextUtils.isEmpty(totalCopiesError)) {
            boolean belowMinimum = totalCopiesError.contains(">=")
                    || totalCopiesError.toLowerCase(Locale.ROOT).contains("greater");
            bookTotalCopiesInputLayout.setError(getString(belowMinimum
                    ? R.string.book_total_copies_minimum
                    : R.string.book_total_copies_required));
        }

        if (!TextUtils.isEmpty(fieldErrors.get("authorId"))) {
            bookAuthorInputLayout.setError(getString(R.string.book_author_required));
        }
        if (!TextUtils.isEmpty(fieldErrors.get("categoryId"))) {
            bookCategoryInputLayout.setError(getString(R.string.book_category_required));
        }
    }

    private boolean isRequiredError(String backendMessage) {
        String normalized = backendMessage.toLowerCase(Locale.ROOT);
        return normalized.contains("required") || normalized.contains("blank");
    }

    private void renderCreateSuccess(BookResponse book) {
        viewModel.consumeCreateSuccess();
        renderMutationSuccess(book, R.string.book_create_success);
    }

    private void renderUpdateSuccess(BookResponse book) {
        viewModel.consumeUpdateSuccess();
        renderMutationSuccess(book, R.string.book_update_success);
    }

    private void renderMutationSuccess(BookResponse book, int messageResource) {
        if (bookFormDialog != null) {
            bookFormDialog.dismiss();
        }

        Snackbar.make(
                requireView(),
                getString(messageResource, bookTitle(book)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.reloadCurrentList();
    }

    private void renderDeleting(Boolean ignored) {
        updateBookActionState();
    }

    private void renderDeleteSuccess(BookResponse book) {
        viewModel.consumeDeleteSuccess();
        Snackbar.make(
                requireView(),
                getString(R.string.book_delete_success, bookTitle(book)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.reloadCurrentList();
    }

    private void renderDeleteError(ApiErrorResponse error) {
        viewModel.consumeDeleteError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.book_delete_failed)
                : message;
        Snackbar.make(requireView(), displayMessage, Snackbar.LENGTH_LONG).show();
    }

    private void updateBookActionState() {
        if (viewModel == null) {
            return;
        }

        boolean busy = Boolean.TRUE.equals(viewModel.getLoading().getValue())
                || Boolean.TRUE.equals(viewModel.getReferenceDataLoading().getValue())
                || Boolean.TRUE.equals(viewModel.getCreating().getValue())
                || Boolean.TRUE.equals(viewModel.getUpdating().getValue())
                || Boolean.TRUE.equals(viewModel.getDeleting().getValue());
        boolean enabled = !busy && referenceDataReady();

        if (addBookButton != null) {
            addBookButton.setEnabled(enabled);
        }
        if (adapter != null) {
            adapter.setActionsEnabled(enabled);
        }
    }

    private boolean referenceDataReady() {
        return !availableAuthors.isEmpty() && !availableCategories.isEmpty();
    }

    private void clearBookFormErrors() {
        bookTitleInputLayout.setError(null);
        bookIsbnInputLayout.setError(null);
        bookPublicationYearInputLayout.setError(null);
        bookTotalCopiesInputLayout.setError(null);
        bookAuthorInputLayout.setError(null);
        bookCategoryInputLayout.setError(null);
        bookFormErrorText.setText(null);
        bookFormErrorText.setVisibility(View.GONE);
    }

    private String bookTitle(BookResponse book) {
        String title = book.getTitle();
        return TextUtils.isEmpty(title)
                ? getString(R.string.book_title_missing)
                : title;
    }

    private String authorName(AuthorResponse author) {
        String firstName = author.getFirstName() == null ? "" : author.getFirstName();
        String lastName = author.getLastName() == null ? "" : author.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isEmpty()
                ? getString(R.string.book_author_missing)
                : fullName;
    }

    private String categoryName(CategoryResponse category) {
        String name = category.getName();
        return TextUtils.isEmpty(name)
                ? getString(R.string.book_category_missing)
                : name;
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = bookFormDialog != null && bookFormDialog.isShowing()
                ? bookFormDialog.getCurrentFocus()
                : requireActivity().getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void clearBookFormReferences() {
        bookFormDialog = null;
        bookTitleInputLayout = null;
        bookTitleInput = null;
        bookIsbnInputLayout = null;
        bookIsbnInput = null;
        bookPublicationYearInputLayout = null;
        bookPublicationYearInput = null;
        bookTotalCopiesInputLayout = null;
        bookTotalCopiesInput = null;
        bookAuthorInputLayout = null;
        bookAuthorInput = null;
        bookCategoryInputLayout = null;
        bookCategoryInput = null;
        bookFormErrorText = null;
        bookFormProgress = null;
        editingBook = null;
        selectedAuthorId = null;
        selectedCategoryId = null;
    }

    @Override
    public void onDestroyView() {
        cancelPendingSearch();
        if (bookSearchInput != null && searchTextWatcher != null) {
            bookSearchInput.removeTextChangedListener(searchTextWatcher);
        }
        searchTextWatcher = null;
        if (bookFormDialog != null) {
            bookFormDialog.dismiss();
        }
        booksRecyclerView.setAdapter(null);
        adapter = null;
        bookSearchInputLayout = null;
        bookSearchInput = null;
        booksRecyclerView = null;
        booksProgress = null;
        booksEmptyText = null;
        booksErrorContainer = null;
        booksErrorText = null;
        booksRetryButton = null;
        addBookButton = null;
        availableAuthors = Collections.emptyList();
        availableCategories = Collections.emptyList();
        super.onDestroyView();
    }
}
