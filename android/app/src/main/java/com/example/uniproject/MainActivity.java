package com.example.uniproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.uniproject.auth.SessionManager;
import com.example.uniproject.auth.SessionExpirationNotifier;
import com.example.uniproject.data.remote.RetrofitProvider;
import com.example.uniproject.ui.auth.login.LoginFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        RetrofitProvider.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configureApplicationShell();
        observeSessionExpiration();
        restoreSessionIfAvailable(savedInstanceState);
    }

    private void configureApplicationShell() {
        NavController navController = findNavController();
        if (navController == null) {
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.appToolbar);
        BottomNavigationView mainNavigation = findViewById(R.id.mainNavigation);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });

        mainNavigation.setOnItemSelectedListener(item ->
                openMainDestination(item.getItemId()));

        navController.addOnDestinationChangedListener((controller, destination, arguments) ->
                renderApplicationShell(toolbar, mainNavigation, destination));
    }

    private void renderApplicationShell(
            MaterialToolbar toolbar,
            BottomNavigationView mainNavigation,
            NavDestination destination
    ) {
        int destinationId = destination.getId();
        boolean authenticatedDestination = destinationId != R.id.loginFragment
                && destinationId != R.id.registerFragment;
        int visibility = authenticatedDestination ? View.VISIBLE : View.GONE;

        toolbar.setVisibility(visibility);
        mainNavigation.setVisibility(visibility);

        if (!authenticatedDestination) {
            return;
        }

        CharSequence label = destination.getLabel();
        toolbar.setTitle(label == null ? getString(R.string.app_name) : label);

        MenuItem navigationItem = mainNavigation.getMenu().findItem(destinationId);
        if (navigationItem != null) {
            navigationItem.setChecked(true);
        }
    }

    private boolean openMainDestination(int destinationId) {
        NavController navController = findNavController();
        if (navController == null) {
            return false;
        }

        NavDestination currentDestination = navController.getCurrentDestination();
        if (currentDestination != null && currentDestination.getId() == destinationId) {
            return true;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(navController.getGraph().getId(), true)
                .setLaunchSingleTop(true)
                .build();
        navController.navigate(destinationId, null, navOptions);
        return true;
    }

    private void observeSessionExpiration() {
        SessionExpirationNotifier.getEvents().observe(this, event -> {
            if (event != null && event.consume()) {
                openLoginAfterSessionExpiration();
            }
        });
    }

    private void restoreSessionIfAvailable(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            return;
        }

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (!sessionManager.hasSession()) {
            return;
        }

        NavController navController = findNavController();
        if (navController == null) {
            return;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .setLaunchSingleTop(true)
                .build();

        navController.navigate(R.id.authorsFragment, null, navOptions);
    }

    private void openLoginAfterSessionExpiration() {
        openLogin(true);
    }

    private void logout() {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        sessionManager.clearSession();
        openLogin(false);
    }

    private void openLogin(boolean sessionExpired) {
        NavController navController = findNavController();
        if (navController == null) {
            return;
        }

        NavOptions navOptions = new NavOptions.Builder()
                .setPopUpTo(navController.getGraph().getId(), true)
                .setLaunchSingleTop(true)
                .build();
        Bundle arguments = new Bundle();
        arguments.putBoolean(
                sessionExpired
                        ? LoginFragment.ARG_SHOW_SESSION_EXPIRED
                        : LoginFragment.ARG_SHOW_LOGOUT_SUCCESS,
                true
        );

        navController.navigate(R.id.loginFragment, arguments, navOptions);
    }

    private NavController findNavController() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (!(fragment instanceof NavHostFragment)) {
            return null;
        }

        return ((NavHostFragment) fragment).getNavController();
    }
}
