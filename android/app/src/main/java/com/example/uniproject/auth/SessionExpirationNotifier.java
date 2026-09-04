package com.example.uniproject.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public final class SessionExpirationNotifier {
    private static final MutableLiveData<SessionExpirationEvent> events =
            new MutableLiveData<>();

    private SessionExpirationNotifier() {
    }

    public static LiveData<SessionExpirationEvent> getEvents() {
        return events;
    }

    public static void notifySessionExpired() {
        events.postValue(new SessionExpirationEvent());
    }

    public static final class SessionExpirationEvent {
        private boolean handled;

        public synchronized boolean consume() {
            if (handled) {
                return false;
            }

            handled = true;
            return true;
        }
    }
}
