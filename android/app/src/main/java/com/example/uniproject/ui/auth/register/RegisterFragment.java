package com.example.uniproject.ui.auth.register;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.uniproject.R;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.data.model.auth.AuthResponse;
import com.example.uniproject.ui.auth.login.LoginFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public final class RegisterFragment extends Fragment {
    private static final int USERNAME_MIN_LENGTH = 3;
    private static final int USERNAME_MAX_LENGTH = 100;
    private static final int EMAIL_MAX_LENGTH = 150;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 100;
    private static final int FULL_NAME_MAX_LENGTH = 150;

    private RegisterViewModel viewModel;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout fullNameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText usernameInput;
    private TextInputEditText emailInput;
    private TextInputEditText fullNameInput;
    private TextInputEditText passwordInput;
    private MaterialButton registerButton;
    private MaterialButton backToLoginButton;
    private ProgressBar registerProgress;
    private TextView registerErrorText;

    public RegisterFragment() {
        super(R.layout.fragment_register);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        bindViews(view);
        bindActions();
        observeViewModel();
    }

    private void bindViews(View view) {
        usernameInputLayout = view.findViewById(R.id.registerUsernameInputLayout);
        emailInputLayout = view.findViewById(R.id.registerEmailInputLayout);
        fullNameInputLayout = view.findViewById(R.id.registerFullNameInputLayout);
        passwordInputLayout = view.findViewById(R.id.registerPasswordInputLayout);
        usernameInput = view.findViewById(R.id.registerUsernameInput);
        emailInput = view.findViewById(R.id.registerEmailInput);
        fullNameInput = view.findViewById(R.id.registerFullNameInput);
        passwordInput = view.findViewById(R.id.registerPasswordInput);
        registerButton = view.findViewById(R.id.registerButton);
        backToLoginButton = view.findViewById(R.id.backToLoginButton);
        registerProgress = view.findViewById(R.id.registerProgress);
        registerErrorText = view.findViewById(R.id.registerErrorText);
    }

    private void bindActions() {
        registerButton.setOnClickListener(ignored -> attemptRegistration());
        backToLoginButton.setOnClickListener(ignored -> NavHostFragment
                .findNavController(this)
                .navigateUp());
        passwordInput.setOnEditorActionListener((ignored, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegistration();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getRegistrationError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderError(error);
            }
        });
        viewModel.getRegistrationSuccess().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                viewModel.consumeRegistrationSuccess();
                returnToLoginAfterSuccess();
            }
        });
    }

    private void attemptRegistration() {
        clearErrors();

        String username = textOf(usernameInput).trim();
        String email = textOf(emailInput).trim();
        String fullName = textOf(fullNameInput).trim();
        String password = textOf(passwordInput);

        View firstInvalidInput = validate(username, email, password, fullName);
        if (firstInvalidInput != null) {
            firstInvalidInput.requestFocus();
            return;
        }

        hideKeyboard();
        viewModel.register(username, email, password, fullName);
    }

    private View validate(String username, String email, String password, String fullName) {
        View firstInvalidInput = null;

        if (TextUtils.isEmpty(username)) {
            usernameInputLayout.setError(getString(R.string.register_username_required));
            firstInvalidInput = usernameInput;
        } else if (username.length() < USERNAME_MIN_LENGTH
                || username.length() > USERNAME_MAX_LENGTH) {
            usernameInputLayout.setError(getString(R.string.register_username_length));
            firstInvalidInput = usernameInput;
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.register_email_required));
            if (firstInvalidInput == null) {
                firstInvalidInput = emailInput;
            }
        } else if (email.length() > EMAIL_MAX_LENGTH) {
            emailInputLayout.setError(getString(R.string.register_email_length));
            if (firstInvalidInput == null) {
                firstInvalidInput = emailInput;
            }
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.register_email_invalid));
            if (firstInvalidInput == null) {
                firstInvalidInput = emailInput;
            }
        }

        if (fullName.length() > FULL_NAME_MAX_LENGTH) {
            fullNameInputLayout.setError(getString(R.string.register_full_name_length));
            if (firstInvalidInput == null) {
                firstInvalidInput = fullNameInput;
            }
        }

        if (TextUtils.isEmpty(password.trim())) {
            passwordInputLayout.setError(getString(R.string.register_password_required));
            if (firstInvalidInput == null) {
                firstInvalidInput = passwordInput;
            }
        } else if (password.length() < PASSWORD_MIN_LENGTH
                || password.length() > PASSWORD_MAX_LENGTH) {
            passwordInputLayout.setError(getString(R.string.register_password_length));
            if (firstInvalidInput == null) {
                firstInvalidInput = passwordInput;
            }
        }

        return firstInvalidInput;
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        registerProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        usernameInput.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        fullNameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        backToLoginButton.setEnabled(!loading);
    }

    private void renderError(ApiErrorResponse error) {
        clearErrors();

        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            setServerFieldError(usernameInputLayout, fieldErrors, "username");
            setServerFieldError(emailInputLayout, fieldErrors, "email");
            setServerFieldError(fullNameInputLayout, fieldErrors, "fullName");
            setServerFieldError(passwordInputLayout, fieldErrors, "password");
        }

        String message = error.getMessage();
        if (!TextUtils.isEmpty(message)) {
            registerErrorText.setText(message);
            registerErrorText.setVisibility(View.VISIBLE);
        }
    }

    private void setServerFieldError(
            TextInputLayout inputLayout,
            Map<String, String> fieldErrors,
            String fieldName
    ) {
        String backendMessage = fieldErrors.get(fieldName);
        if (!TextUtils.isEmpty(backendMessage)) {
            inputLayout.setError(localizedFieldError(fieldName, backendMessage));
        }
    }

    private String localizedFieldError(String fieldName, String backendMessage) {
        boolean blank = backendMessage.toLowerCase().contains("blank");
        switch (fieldName) {
            case "username":
                return getString(blank
                        ? R.string.register_username_required
                        : R.string.register_username_length);
            case "email":
                if (blank) {
                    return getString(R.string.register_email_required);
                }
                return getString(backendMessage.toLowerCase().contains("size")
                        ? R.string.register_email_length
                        : R.string.register_email_invalid);
            case "password":
                return getString(blank
                        ? R.string.register_password_required
                        : R.string.register_password_length);
            case "fullName":
                return getString(R.string.register_full_name_length);
            default:
                return backendMessage;
        }
    }

    private void returnToLoginAfterSuccess() {
        clearErrors();
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry previousEntry = navController.getPreviousBackStackEntry();

        if (previousEntry == null) {
            Snackbar.make(requireView(), R.string.register_success, Snackbar.LENGTH_LONG).show();
            return;
        }

        previousEntry.getSavedStateHandle().set(
                LoginFragment.REGISTRATION_SUCCESS_KEY,
                getString(R.string.register_success)
        );
        navController.navigateUp();
    }

    private void clearErrors() {
        usernameInputLayout.setError(null);
        emailInputLayout.setError(null);
        fullNameInputLayout.setError(null);
        passwordInputLayout.setError(null);
        registerErrorText.setText(null);
        registerErrorText.setVisibility(View.GONE);
    }

    private String textOf(TextInputEditText input) {
        if (input.getText() == null) {
            return "";
        }
        return input.getText().toString();
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if (focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            focusedView.clearFocus();
        }
    }
}
