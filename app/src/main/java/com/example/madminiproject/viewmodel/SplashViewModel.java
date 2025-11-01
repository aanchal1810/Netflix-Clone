package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashViewModel extends ViewModel {

    private final SingleLiveEvent<Boolean> isUserLoggedIn = new SingleLiveEvent<>();

    public LiveData<Boolean> getIsUserLoggedIn() {
        return isUserLoggedIn;
    }

    public void checkUserLoggedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        isUserLoggedIn.setValue(currentUser != null);
    }
}
