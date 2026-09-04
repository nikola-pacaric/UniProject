package com.example.uniproject.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.uniproject.data.model.auth.AuthResponse;

public class SessionManager {
    private static final String PREFERENCES_NAME = "librarian_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(AuthResponse authResponse) {
        preferences.edit()
                .putString(KEY_TOKEN, authResponse.getToken())
                .putString(KEY_USERNAME, authResponse.getUsername())
                .putString(KEY_FULL_NAME, authResponse.getFullName())
                .apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, null);
    }

    public boolean hasSession() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
