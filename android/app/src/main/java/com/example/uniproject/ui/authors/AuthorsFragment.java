package com.example.uniproject.ui.authors;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uniproject.R;
import com.example.uniproject.auth.SessionManager;
import com.google.android.material.snackbar.Snackbar;

public final class AuthorsFragment extends Fragment {
    public static final String ARG_SHOW_LOGIN_SUCCESS = "showLoginSuccess";

    private boolean loginConfirmationShown;

    public AuthorsFragment() {
        super(R.layout.fragment_authors);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());
        String displayName = sessionManager.getFullName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = sessionManager.getUsername();
        }

        TextView welcomeText = view.findViewById(R.id.authorsWelcomeText);
        if (TextUtils.isEmpty(displayName)) {
            welcomeText.setVisibility(View.GONE);
        } else {
            welcomeText.setText(getString(R.string.authors_welcome, displayName));

            Bundle arguments = getArguments();
            boolean shouldShowLoginSuccess = arguments != null
                    && arguments.getBoolean(ARG_SHOW_LOGIN_SUCCESS, false);
            if (shouldShowLoginSuccess && !loginConfirmationShown) {
                loginConfirmationShown = true;
                Snackbar.make(
                        view,
                        getString(R.string.login_success, displayName),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        }
    }
}
