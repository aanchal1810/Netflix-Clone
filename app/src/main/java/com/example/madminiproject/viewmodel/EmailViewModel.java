package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class EmailViewModel extends ViewModel {

    private final SingleLiveEvent<String> navigateToPassword = new SingleLiveEvent<>();

    public LiveData<String> getNavigateToPassword() {
        return navigateToPassword;
    }

    public boolean onNextClicked(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        navigateToPassword.setValue(email.trim());
        return true;
    }
}
