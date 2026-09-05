package com.example.uniproject.ui.authors;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.example.uniproject.data.model.author.AuthorResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Map;

public final class AuthorsFragment extends Fragment {
    public static final String ARG_SHOW_LOGIN_SUCCESS = "showLoginSuccess";
    private static final int AUTHOR_NAME_MAX_LENGTH = 100;

    private boolean loginConfirmationShown;
    private AuthorsViewModel viewModel;
    private AuthorAdapter adapter;
    private RecyclerView authorsRecyclerView;
    private ProgressBar authorsProgress;
    private TextView authorsEmptyText;
    private View authorsErrorContainer;
    private TextView authorsErrorText;
    private MaterialButton authorsRetryButton;
    private FloatingActionButton addAuthorButton;

    private AlertDialog createAuthorDialog;
    private TextInputLayout firstNameInputLayout;
    private TextInputLayout lastNameInputLayout;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText biographyInput;
    private ProgressBar authorFormProgress;
    private TextView authorFormErrorText;
    private AuthorResponse editingAuthor;

    public AuthorsFragment() {
        super(R.layout.fragment_authors);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AuthorsViewModel.class);
        bindViews(view);
        configureList();
        observeViewModel();
        authorsRetryButton.setOnClickListener(ignored -> viewModel.loadAuthors());
        addAuthorButton.setOnClickListener(ignored -> showCreateAuthorDialog());

        SessionManager sessionManager = new SessionManager(requireContext());
        String displayName = sessionManager.getFullName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = sessionManager.getUsername();
        }

        Bundle arguments = getArguments();
        boolean shouldShowLoginSuccess = arguments != null
                && arguments.getBoolean(ARG_SHOW_LOGIN_SUCCESS, false);
        if (!TextUtils.isEmpty(displayName)
                && shouldShowLoginSuccess
                && !loginConfirmationShown) {
            loginConfirmationShown = true;
            Snackbar.make(
                    view,
                    getString(R.string.login_success, displayName),
                    Snackbar.LENGTH_LONG
            ).show();
        }

        if (viewModel.getAuthors().getValue() == null
                && viewModel.getLoadError().getValue() == null) {
            viewModel.loadAuthors();
        }
    }

    private void bindViews(View view) {
        authorsRecyclerView = view.findViewById(R.id.authorsRecyclerView);
        authorsProgress = view.findViewById(R.id.authorsProgress);
        authorsEmptyText = view.findViewById(R.id.authorsEmptyText);
        authorsErrorContainer = view.findViewById(R.id.authorsErrorContainer);
        authorsErrorText = view.findViewById(R.id.authorsErrorText);
        authorsRetryButton = view.findViewById(R.id.authorsRetryButton);
        addAuthorButton = view.findViewById(R.id.addAuthorButton);
    }

    private void configureList() {
        adapter = new AuthorAdapter(
                this::showEditAuthorDialog,
                this::showDeleteAuthorConfirmation
        );
        authorsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        authorsRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getAuthors().observe(getViewLifecycleOwner(), loadedAuthors -> {
            if (loadedAuthors != null) {
                renderAuthors(loadedAuthors);
            }
        });
        viewModel.getLoadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderError(error);
            }
        });
        viewModel.getCreating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getCreateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderAuthorFormError(error);
            }
        });
        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                renderCreateSuccess(author);
            }
        });
        viewModel.getUpdating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getUpdateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderAuthorFormError(error);
            }
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                renderUpdateSuccess(author);
            }
        });
        viewModel.getDeleting().observe(getViewLifecycleOwner(), this::renderDeleting);
        viewModel.getDeleteError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderDeleteError(error);
            }
        });
        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), author -> {
            if (author != null) {
                renderDeleteSuccess(author);
            }
        });
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        authorsProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            authorsRecyclerView.setVisibility(View.GONE);
            authorsEmptyText.setVisibility(View.GONE);
            authorsErrorContainer.setVisibility(View.GONE);
        }
        updateAddAuthorButtonState();
    }

    private void renderAuthors(List<AuthorResponse> loadedAuthors) {
        adapter.submitList(loadedAuthors);
        authorsProgress.setVisibility(View.GONE);
        authorsErrorContainer.setVisibility(View.GONE);

        boolean empty = loadedAuthors.isEmpty();
        authorsEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        authorsRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderError(ApiErrorResponse error) {
        authorsProgress.setVisibility(View.GONE);
        authorsRecyclerView.setVisibility(View.GONE);
        authorsEmptyText.setVisibility(View.GONE);
        authorsErrorContainer.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        authorsErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.authors_load_failed)
                : message);
    }

    private void showCreateAuthorDialog() {
        showAuthorFormDialog(null);
    }

    private void showEditAuthorDialog(AuthorResponse author) {
        showAuthorFormDialog(author);
    }

    private void showDeleteAuthorConfirmation(AuthorResponse author) {
        String fullName = authorFullName(author);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_author_title)
                .setMessage(getString(R.string.delete_author_confirmation, fullName))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(
                        R.string.delete_author_action,
                        (dialog, which) -> viewModel.deleteAuthor(author)
                )
                .show();
    }

    private void showAuthorFormDialog(@Nullable AuthorResponse author) {
        if (createAuthorDialog != null && createAuthorDialog.isShowing()) {
            return;
        }

        editingAuthor = author;
        if (editingAuthor == null) {
            viewModel.prepareCreate();
        } else {
            viewModel.prepareUpdate();
        }

        View formView = getLayoutInflater().inflate(R.layout.dialog_author_form, null, false);
        bindAuthorForm(formView);
        populateAuthorForm(editingAuthor);

        createAuthorDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingAuthor == null
                        ? R.string.create_author_title
                        : R.string.edit_author_title)
                .setView(formView)
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.author_create_action, null)
                .create();

        createAuthorDialog.setOnShowListener(ignored -> {
            Button saveButton = createAuthorDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(button -> attemptSaveAuthor());
            renderSaving(null);
            firstNameInput.requestFocus();
            firstNameInput.setSelection(firstNameInput.length());
        });
        createAuthorDialog.setOnDismissListener(ignored -> clearAuthorFormReferences());
        createAuthorDialog.show();
    }

    private void bindAuthorForm(View formView) {
        firstNameInputLayout = formView.findViewById(R.id.authorFirstNameInputLayout);
        lastNameInputLayout = formView.findViewById(R.id.authorLastNameInputLayout);
        firstNameInput = formView.findViewById(R.id.authorFirstNameInput);
        lastNameInput = formView.findViewById(R.id.authorLastNameInput);
        biographyInput = formView.findViewById(R.id.authorBiographyInput);
        authorFormProgress = formView.findViewById(R.id.authorFormProgress);
        authorFormErrorText = formView.findViewById(R.id.authorFormErrorText);
    }

    private void populateAuthorForm(@Nullable AuthorResponse author) {
        if (author == null) {
            return;
        }
        firstNameInput.setText(author.getFirstName());
        lastNameInput.setText(author.getLastName());
        biographyInput.setText(author.getBiography());
    }

    private void attemptSaveAuthor() {
        clearAuthorFormErrors();

        String firstName = textOf(firstNameInput).trim();
        String lastName = textOf(lastNameInput).trim();
        String biography = textOf(biographyInput).trim();

        View firstInvalidInput = validateAuthor(firstName, lastName);
        if (firstInvalidInput != null) {
            firstInvalidInput.requestFocus();
            return;
        }

        hideKeyboard();
        String optionalBiography = TextUtils.isEmpty(biography) ? null : biography;
        if (editingAuthor == null) {
            viewModel.createAuthor(firstName, lastName, optionalBiography);
        } else {
            viewModel.updateAuthor(
                    editingAuthor.getId(),
                    firstName,
                    lastName,
                    optionalBiography
            );
        }
    }

    private View validateAuthor(String firstName, String lastName) {
        View firstInvalidInput = null;

        if (TextUtils.isEmpty(firstName)) {
            firstNameInputLayout.setError(getString(R.string.author_first_name_required));
            firstInvalidInput = firstNameInput;
        } else if (firstName.length() > AUTHOR_NAME_MAX_LENGTH) {
            firstNameInputLayout.setError(getString(R.string.author_name_length));
            firstInvalidInput = firstNameInput;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameInputLayout.setError(getString(R.string.author_last_name_required));
            if (firstInvalidInput == null) {
                firstInvalidInput = lastNameInput;
            }
        } else if (lastName.length() > AUTHOR_NAME_MAX_LENGTH) {
            lastNameInputLayout.setError(getString(R.string.author_name_length));
            if (firstInvalidInput == null) {
                firstInvalidInput = lastNameInput;
            }
        }

        return firstInvalidInput;
    }

    private void renderSaving(Boolean ignored) {
        updateAddAuthorButtonState();
        if (createAuthorDialog == null || !createAuthorDialog.isShowing()) {
            return;
        }

        boolean saving = Boolean.TRUE.equals(viewModel.getCreating().getValue())
                || Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        firstNameInput.setEnabled(!saving);
        lastNameInput.setEnabled(!saving);
        biographyInput.setEnabled(!saving);
        authorFormProgress.setVisibility(saving ? View.VISIBLE : View.GONE);
        createAuthorDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!saving);
        createAuthorDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!saving);
        createAuthorDialog.setCanceledOnTouchOutside(!saving);
    }

    private void renderAuthorFormError(ApiErrorResponse error) {
        if (createAuthorDialog == null || !createAuthorDialog.isShowing()) {
            return;
        }

        clearAuthorFormErrors();
        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            setAuthorFieldError(firstNameInputLayout, fieldErrors, "firstName");
            setAuthorFieldError(lastNameInputLayout, fieldErrors, "lastName");
        }

        String message = error.getMessage();
        authorFormErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.author_create_failed)
                : message);
        authorFormErrorText.setVisibility(View.VISIBLE);
    }

    private void setAuthorFieldError(
            TextInputLayout inputLayout,
            Map<String, String> fieldErrors,
            String fieldName
    ) {
        String backendMessage = fieldErrors.get(fieldName);
        if (TextUtils.isEmpty(backendMessage)) {
            return;
        }

        boolean blank = backendMessage.toLowerCase().contains("required")
                || backendMessage.toLowerCase().contains("blank");
        if ("firstName".equals(fieldName) && blank) {
            inputLayout.setError(getString(R.string.author_first_name_required));
        } else if ("lastName".equals(fieldName) && blank) {
            inputLayout.setError(getString(R.string.author_last_name_required));
        } else {
            inputLayout.setError(getString(R.string.author_name_length));
        }
    }

    private void renderCreateSuccess(AuthorResponse author) {
        viewModel.consumeCreateSuccess();
        renderMutationSuccess(author, R.string.author_create_success);
    }

    private void renderUpdateSuccess(AuthorResponse author) {
        viewModel.consumeUpdateSuccess();
        renderMutationSuccess(author, R.string.author_update_success);
    }

    private void renderMutationSuccess(AuthorResponse author, int messageResource) {
        if (createAuthorDialog != null) {
            createAuthorDialog.dismiss();
        }

        Snackbar.make(
                requireView(),
                getString(messageResource, authorFullName(author)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadAuthors();
    }

    private void renderDeleting(Boolean deletingValue) {
        boolean deleting = Boolean.TRUE.equals(deletingValue);
        adapter.setActionsEnabled(!deleting);
        updateAddAuthorButtonState();
    }

    private void renderDeleteSuccess(AuthorResponse author) {
        viewModel.consumeDeleteSuccess();
        Snackbar.make(
                requireView(),
                getString(R.string.author_delete_success, authorFullName(author)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadAuthors();
    }

    private void renderDeleteError(ApiErrorResponse error) {
        viewModel.consumeDeleteError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.author_delete_failed)
                : message;
        Snackbar.make(
                requireView(),
                displayMessage,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private String authorFullName(AuthorResponse author) {
        String firstName = author.getFirstName() == null ? "" : author.getFirstName();
        String lastName = author.getLastName() == null ? "" : author.getLastName();
        return (firstName + " " + lastName).trim();
    }

    private void updateAddAuthorButtonState() {
        if (addAuthorButton == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean creating = Boolean.TRUE.equals(viewModel.getCreating().getValue());
        boolean updating = Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        boolean deleting = Boolean.TRUE.equals(viewModel.getDeleting().getValue());
        addAuthorButton.setEnabled(!loading && !creating && !updating && !deleting);
    }

    private void clearAuthorFormErrors() {
        firstNameInputLayout.setError(null);
        lastNameInputLayout.setError(null);
        authorFormErrorText.setText(null);
        authorFormErrorText.setVisibility(View.GONE);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = createAuthorDialog == null
                ? null
                : createAuthorDialog.getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void clearAuthorFormReferences() {
        createAuthorDialog = null;
        firstNameInputLayout = null;
        lastNameInputLayout = null;
        firstNameInput = null;
        lastNameInput = null;
        biographyInput = null;
        authorFormProgress = null;
        authorFormErrorText = null;
        editingAuthor = null;
    }

    @Override
    public void onDestroyView() {
        if (createAuthorDialog != null) {
            createAuthorDialog.dismiss();
        }
        authorsRecyclerView.setAdapter(null);
        adapter = null;
        authorsRecyclerView = null;
        authorsProgress = null;
        authorsEmptyText = null;
        authorsErrorContainer = null;
        authorsErrorText = null;
        authorsRetryButton = null;
        addAuthorButton = null;
        super.onDestroyView();
    }
}
