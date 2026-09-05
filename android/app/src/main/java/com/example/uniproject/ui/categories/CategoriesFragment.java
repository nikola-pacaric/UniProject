package com.example.uniproject.ui.categories;

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
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.category.CategoryResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Map;

public final class CategoriesFragment extends Fragment {
    private static final int CATEGORY_NAME_MAX_LENGTH = 100;

    private CategoriesViewModel viewModel;
    private CategoryAdapter adapter;
    private RecyclerView categoriesRecyclerView;
    private ProgressBar categoriesProgress;
    private TextView categoriesEmptyText;
    private View categoriesErrorContainer;
    private TextView categoriesErrorText;
    private MaterialButton categoriesRetryButton;
    private FloatingActionButton addCategoryButton;

    private AlertDialog categoryFormDialog;
    private TextInputLayout categoryNameInputLayout;
    private TextInputEditText categoryNameInput;
    private TextInputEditText categoryDescriptionInput;
    private ProgressBar categoryFormProgress;
    private TextView categoryFormErrorText;
    private CategoryResponse editingCategory;

    public CategoriesFragment() {
        super(R.layout.fragment_categories);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        bindViews(view);
        configureList();
        observeViewModel();
        categoriesRetryButton.setOnClickListener(ignored -> viewModel.loadCategories());
        addCategoryButton.setOnClickListener(ignored -> showCreateCategoryDialog());

        if (viewModel.getCategories().getValue() == null
                && viewModel.getLoadError().getValue() == null) {
            viewModel.loadCategories();
        }
    }

    private void bindViews(View view) {
        categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
        categoriesProgress = view.findViewById(R.id.categoriesProgress);
        categoriesEmptyText = view.findViewById(R.id.categoriesEmptyText);
        categoriesErrorContainer = view.findViewById(R.id.categoriesErrorContainer);
        categoriesErrorText = view.findViewById(R.id.categoriesErrorText);
        categoriesRetryButton = view.findViewById(R.id.categoriesRetryButton);
        addCategoryButton = view.findViewById(R.id.addCategoryButton);
    }

    private void configureList() {
        adapter = new CategoryAdapter(
                this::showEditCategoryDialog,
                this::showDeleteCategoryConfirmation
        );
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        categoriesRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getCategories().observe(getViewLifecycleOwner(), loadedCategories -> {
            if (loadedCategories != null) {
                renderCategories(loadedCategories);
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
                renderCategoryFormError(error);
            }
        });
        viewModel.getCreateSuccess().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                renderCreateSuccess(category);
            }
        });
        viewModel.getUpdating().observe(getViewLifecycleOwner(), this::renderSaving);
        viewModel.getUpdateError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderCategoryFormError(error);
            }
        });
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                renderUpdateSuccess(category);
            }
        });
        viewModel.getDeleting().observe(getViewLifecycleOwner(), this::renderDeleting);
        viewModel.getDeleteError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderDeleteError(error);
            }
        });
        viewModel.getDeleteSuccess().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                renderDeleteSuccess(category);
            }
        });
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        categoriesProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            categoriesRecyclerView.setVisibility(View.GONE);
            categoriesEmptyText.setVisibility(View.GONE);
            categoriesErrorContainer.setVisibility(View.GONE);
        }
        updateAddCategoryButtonState();
    }

    private void renderCategories(List<CategoryResponse> loadedCategories) {
        adapter.submitList(loadedCategories);
        categoriesProgress.setVisibility(View.GONE);
        categoriesErrorContainer.setVisibility(View.GONE);

        boolean empty = loadedCategories.isEmpty();
        categoriesEmptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        categoriesRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderError(ApiErrorResponse error) {
        categoriesProgress.setVisibility(View.GONE);
        categoriesRecyclerView.setVisibility(View.GONE);
        categoriesEmptyText.setVisibility(View.GONE);
        categoriesErrorContainer.setVisibility(View.VISIBLE);

        String message = error.getMessage();
        categoriesErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.categories_load_failed)
                : message);
    }

    private void showCreateCategoryDialog() {
        showCategoryFormDialog(null);
    }

    private void showEditCategoryDialog(CategoryResponse category) {
        showCategoryFormDialog(category);
    }

    private void showDeleteCategoryConfirmation(CategoryResponse category) {
        String name = categoryName(category);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_category_title)
                .setMessage(getString(R.string.delete_category_confirmation, name))
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(
                        R.string.delete_category_action,
                        (dialog, which) -> viewModel.deleteCategory(category)
                )
                .show();
    }

    private void showCategoryFormDialog(@Nullable CategoryResponse category) {
        if (categoryFormDialog != null && categoryFormDialog.isShowing()) {
            return;
        }

        editingCategory = category;
        if (editingCategory == null) {
            viewModel.prepareCreate();
        } else {
            viewModel.prepareUpdate();
        }

        View formView = getLayoutInflater().inflate(R.layout.dialog_category_form, null, false);
        bindCategoryForm(formView);
        populateCategoryForm(editingCategory);

        categoryFormDialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editingCategory == null
                        ? R.string.create_category_title
                        : R.string.edit_category_title)
                .setView(formView)
                .setNegativeButton(R.string.cancel_action, null)
                .setPositiveButton(R.string.category_create_action, null)
                .create();

        categoryFormDialog.setOnShowListener(ignored -> {
            Button saveButton = categoryFormDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(button -> attemptSaveCategory());
            renderSaving(null);
            categoryNameInput.requestFocus();
            categoryNameInput.setSelection(categoryNameInput.length());
        });
        categoryFormDialog.setOnDismissListener(ignored -> clearCategoryFormReferences());
        categoryFormDialog.show();
    }

    private void bindCategoryForm(View formView) {
        categoryNameInputLayout = formView.findViewById(R.id.categoryNameInputLayout);
        categoryNameInput = formView.findViewById(R.id.categoryNameInput);
        categoryDescriptionInput = formView.findViewById(R.id.categoryDescriptionInput);
        categoryFormProgress = formView.findViewById(R.id.categoryFormProgress);
        categoryFormErrorText = formView.findViewById(R.id.categoryFormErrorText);
    }

    private void populateCategoryForm(@Nullable CategoryResponse category) {
        if (category == null) {
            return;
        }
        categoryNameInput.setText(category.getName());
        categoryDescriptionInput.setText(category.getDescription());
    }

    private void attemptSaveCategory() {
        clearCategoryFormErrors();

        String name = textOf(categoryNameInput).trim();
        String description = textOf(categoryDescriptionInput).trim();

        if (!validateCategoryName(name)) {
            categoryNameInput.requestFocus();
            return;
        }

        hideKeyboard();
        String optionalDescription = TextUtils.isEmpty(description) ? null : description;
        if (editingCategory == null) {
            viewModel.createCategory(name, optionalDescription);
        } else {
            viewModel.updateCategory(editingCategory.getId(), name, optionalDescription);
        }
    }

    private boolean validateCategoryName(String name) {
        if (TextUtils.isEmpty(name)) {
            categoryNameInputLayout.setError(getString(R.string.category_name_required));
            return false;
        }
        if (name.length() > CATEGORY_NAME_MAX_LENGTH) {
            categoryNameInputLayout.setError(getString(R.string.category_name_length));
            return false;
        }
        return true;
    }

    private void renderSaving(Boolean ignored) {
        updateAddCategoryButtonState();
        if (categoryFormDialog == null || !categoryFormDialog.isShowing()) {
            return;
        }

        boolean saving = Boolean.TRUE.equals(viewModel.getCreating().getValue())
                || Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        categoryNameInput.setEnabled(!saving);
        categoryDescriptionInput.setEnabled(!saving);
        categoryFormProgress.setVisibility(saving ? View.VISIBLE : View.GONE);
        categoryFormDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!saving);
        categoryFormDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(!saving);
        categoryFormDialog.setCanceledOnTouchOutside(!saving);
    }

    private void renderCategoryFormError(ApiErrorResponse error) {
        if (categoryFormDialog == null || !categoryFormDialog.isShowing()) {
            return;
        }

        clearCategoryFormErrors();
        if (error.getStatus() == 409) {
            categoryNameInputLayout.setError(getString(R.string.category_name_duplicate));
            categoryNameInput.requestFocus();
            return;
        }

        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            setCategoryNameFieldError(fieldErrors);
        }

        String message = error.getMessage();
        categoryFormErrorText.setText(TextUtils.isEmpty(message)
                ? getString(R.string.category_create_failed)
                : message);
        categoryFormErrorText.setVisibility(View.VISIBLE);
    }

    private void setCategoryNameFieldError(Map<String, String> fieldErrors) {
        String backendMessage = fieldErrors.get("name");
        if (TextUtils.isEmpty(backendMessage)) {
            return;
        }

        boolean blank = backendMessage.toLowerCase().contains("required")
                || backendMessage.toLowerCase().contains("blank");
        categoryNameInputLayout.setError(getString(blank
                ? R.string.category_name_required
                : R.string.category_name_length));
    }

    private void renderCreateSuccess(CategoryResponse category) {
        viewModel.consumeCreateSuccess();
        renderMutationSuccess(category, R.string.category_create_success);
    }

    private void renderUpdateSuccess(CategoryResponse category) {
        viewModel.consumeUpdateSuccess();
        renderMutationSuccess(category, R.string.category_update_success);
    }

    private void renderMutationSuccess(CategoryResponse category, int messageResource) {
        if (categoryFormDialog != null) {
            categoryFormDialog.dismiss();
        }

        Snackbar.make(
                requireView(),
                getString(messageResource, categoryName(category)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadCategories();
    }

    private void renderDeleting(Boolean deletingValue) {
        boolean deleting = Boolean.TRUE.equals(deletingValue);
        adapter.setActionsEnabled(!deleting);
        updateAddCategoryButtonState();
    }

    private void renderDeleteSuccess(CategoryResponse category) {
        viewModel.consumeDeleteSuccess();
        Snackbar.make(
                requireView(),
                getString(R.string.category_delete_success, categoryName(category)),
                Snackbar.LENGTH_LONG
        ).show();
        viewModel.loadCategories();
    }

    private void renderDeleteError(ApiErrorResponse error) {
        viewModel.consumeDeleteError();
        String message = error.getMessage();
        String displayMessage = TextUtils.isEmpty(message)
                ? getString(R.string.category_delete_failed)
                : message;
        Snackbar.make(
                requireView(),
                displayMessage,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private String categoryName(CategoryResponse category) {
        return category.getName() == null ? "" : category.getName();
    }

    private void updateAddCategoryButtonState() {
        if (addCategoryButton == null) {
            return;
        }
        boolean loading = Boolean.TRUE.equals(viewModel.getLoading().getValue());
        boolean creating = Boolean.TRUE.equals(viewModel.getCreating().getValue());
        boolean updating = Boolean.TRUE.equals(viewModel.getUpdating().getValue());
        boolean deleting = Boolean.TRUE.equals(viewModel.getDeleting().getValue());
        addCategoryButton.setEnabled(!loading && !creating && !updating && !deleting);
    }

    private void clearCategoryFormErrors() {
        categoryNameInputLayout.setError(null);
        categoryFormErrorText.setText(null);
        categoryFormErrorText.setVisibility(View.GONE);
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = categoryFormDialog == null
                ? null
                : categoryFormDialog.getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }

    private void clearCategoryFormReferences() {
        categoryFormDialog = null;
        categoryNameInputLayout = null;
        categoryNameInput = null;
        categoryDescriptionInput = null;
        categoryFormProgress = null;
        categoryFormErrorText = null;
        editingCategory = null;
    }

    @Override
    public void onDestroyView() {
        if (categoryFormDialog != null) {
            categoryFormDialog.dismiss();
        }
        categoriesRecyclerView.setAdapter(null);
        adapter = null;
        categoriesRecyclerView = null;
        categoriesProgress = null;
        categoriesEmptyText = null;
        categoriesErrorContainer = null;
        categoriesErrorText = null;
        categoriesRetryButton = null;
        addCategoryButton = null;
        super.onDestroyView();
    }
}
