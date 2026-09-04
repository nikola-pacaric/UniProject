package com.example.uniproject.ui.auth.login;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.uniproject.R;
import com.example.uniproject.data.http.ApiErrorResponse;
import com.example.uniproject.ui.authors.AuthorsFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class LoginFragment extends Fragment {
    public static final String REGISTRATION_SUCCESS_KEY = "registrationSuccess";
    public static final String ARG_SHOW_LOGOUT_SUCCESS = "showLogoutSuccess";
    public static final String ARG_SHOW_SESSION_EXPIRED = "showSessionExpired";

    private LoginViewModel viewModel;
    private boolean logoutConfirmationShown;
    private boolean sessionExpiredMessageShown;
    private TextInputLayout usernameInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText usernameInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private ProgressBar loginProgress;
    private TextView loginErrorText;
    private MaterialButton openRegistrationButton;

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        bindViews(view);
        bindActions();
        observeViewModel();
        showSessionFeedbackIfNeeded(view);
    }

    private void bindViews(View view) {
        usernameInputLayout = view.findViewById(R.id.usernameInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);
        usernameInput = view.findViewById(R.id.usernameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        loginProgress = view.findViewById(R.id.loginProgress);
        loginErrorText = view.findViewById(R.id.loginErrorText);
        openRegistrationButton = view.findViewById(R.id.openRegistrationButton);
    }

    private void bindActions() {
        loginButton.setOnClickListener(ignored -> attemptLogin());
        openRegistrationButton.setOnClickListener(ignored -> NavHostFragment
                .findNavController(this)
                .navigate(R.id.action_loginFragment_to_registerFragment));
        passwordInput.setOnEditorActionListener((ignored, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::renderLoading);
        viewModel.getLoginError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                renderError(error);
            }
        });
        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), response -> {
            if (response != null) {
                viewModel.consumeLoginSuccess();
                openAuthenticatedArea();
            }
        });

        observeRegistrationResult();
    }

    private void observeRegistrationResult() {
        NavController navController = NavHostFragment.findNavController(this);
        NavBackStackEntry currentEntry = navController.getCurrentBackStackEntry();
        if (currentEntry == null) {
            return;
        }

        currentEntry.getSavedStateHandle()
                .<String>getLiveData(REGISTRATION_SUCCESS_KEY)
                .observe(getViewLifecycleOwner(), message -> {
                    if (!TextUtils.isEmpty(message)) {
                        Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show();
                        currentEntry.getSavedStateHandle().remove(REGISTRATION_SUCCESS_KEY);
                    }
                });
    }

    private void showSessionFeedbackIfNeeded(View view) {
        Bundle arguments = getArguments();
        boolean shouldShowSessionExpired = arguments != null
                && arguments.getBoolean(ARG_SHOW_SESSION_EXPIRED, false);
        if (shouldShowSessionExpired && !sessionExpiredMessageShown) {
            sessionExpiredMessageShown = true;
            Snackbar.make(view, R.string.session_expired, Snackbar.LENGTH_LONG).show();
            return;
        }

        boolean shouldShowLogoutSuccess = arguments != null
                && arguments.getBoolean(ARG_SHOW_LOGOUT_SUCCESS, false);
        if (shouldShowLogoutSuccess && !logoutConfirmationShown) {
            logoutConfirmationShown = true;
            Snackbar.make(view, R.string.logout_success, Snackbar.LENGTH_LONG).show();
        }
    }

    private void attemptLogin() {
        clearErrors();

        String username = textOf(usernameInput).trim();
        String password = textOf(passwordInput);
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            usernameInputLayout.setError(getString(R.string.login_username_required));
            valid = false;
        }

        if (TextUtils.isEmpty(password.trim())) {
            passwordInputLayout.setError(getString(R.string.login_password_required));
            valid = false;
        }

        if (!valid) {
            if (TextUtils.isEmpty(username)) {
                usernameInput.requestFocus();
            } else {
                passwordInput.requestFocus();
            }
            return;
        }

        hideKeyboard();
        viewModel.login(username, password);
    }

    private void renderLoading(Boolean loadingValue) {
        boolean loading = Boolean.TRUE.equals(loadingValue);
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        usernameInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        loginButton.setEnabled(!loading);
        openRegistrationButton.setEnabled(!loading);
    }

    private void renderError(ApiErrorResponse error) {
        clearErrors();

        Map<String, String> fieldErrors = error.getFieldErrors();
        if (fieldErrors != null) {
            usernameInputLayout.setError(fieldErrors.get("username"));
            passwordInputLayout.setError(fieldErrors.get("password"));
        }

        String message = error.getMessage();
        if (!TextUtils.isEmpty(message)) {
            loginErrorText.setText(message);
            loginErrorText.setVisibility(View.VISIBLE);
        }
    }

    private void openAuthenticatedArea() {
        clearErrors();
        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .setLaunchSingleTop(true)
                .build();

        Bundle arguments = new Bundle();
        arguments.putBoolean(AuthorsFragment.ARG_SHOW_LOGIN_SUCCESS, true);

        NavHostFragment.findNavController(this)
                .navigate(R.id.authorsFragment, arguments, navOptions);
    }

    private void clearErrors() {
        usernameInputLayout.setError(null);
        passwordInputLayout.setError(null);
        loginErrorText.setText(null);
        loginErrorText.setVisibility(View.GONE);
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
