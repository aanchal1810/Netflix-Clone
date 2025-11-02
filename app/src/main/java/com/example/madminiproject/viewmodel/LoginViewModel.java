package com.example.madminiproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends ViewModel {

    private final FirebaseAuth mAuth;
    private final MutableLiveData<AuthResult> authResult = new MutableLiveData<>();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public void authenticateUser(String email, String password) {
        authResult.setValue(new AuthResult.Loading());
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        authResult.setValue(new AuthResult.Success(mAuth.getCurrentUser()));
                    } else {
                        // If sign-in fails, try to sign up the user
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(signupTask -> {
                                    if (signupTask.isSuccessful()) {
                                        authResult.setValue(new AuthResult.NewUser(mAuth.getCurrentUser()));
                                    } else {
                                        authResult.setValue(new AuthResult.Error(signupTask.getException()));
                                    }
                                });
                    }
                });
    }

    public static abstract class AuthResult {
        private AuthResult() {}

        public static final class Loading extends AuthResult {}

        public static final class Success extends AuthResult {
            private final FirebaseUser user;

            public Success(FirebaseUser user) {
                this.user = user;
            }

            public FirebaseUser getUser() {
                return user;
            }
        }

        public static final class NewUser extends AuthResult {
            private final FirebaseUser user;

            public NewUser(FirebaseUser user) {
                this.user = user;
            }

            public FirebaseUser getUser() {
                return user;
            }
        }

        public static final class Error extends AuthResult {
            private final Exception exception;

            public Error(Exception exception) {
                this.exception = exception;
            }

            public Exception getException() {
                return exception;
            }
        }
    }
}
