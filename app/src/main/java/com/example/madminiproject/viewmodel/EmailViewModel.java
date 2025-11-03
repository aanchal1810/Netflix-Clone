package com.example.madminiproject.viewmodel;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class EmailViewModel extends ViewModel {

    private final SingleLiveEvent<String> navigateToPassword = new SingleLiveEvent<>();

    public LiveData<String> getNavigateToPassword() {
        return navigateToPassword;
    }

    public boolean onNextClicked(String email) {
        if (email == null || email.trim().isEmpty() || !isEmailValid(email)) {
            return false;
        }
        navigateToPassword.setValue(email.trim());
        return true;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
