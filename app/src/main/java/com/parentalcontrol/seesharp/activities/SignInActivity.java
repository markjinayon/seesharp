package com.parentalcontrol.seesharp.activities;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.child.ChildDashboardActivity;
import com.parentalcontrol.seesharp.activities.parent.ParentDashboardActivity;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.firebase.FirebaseMethod;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class SignInActivity extends AppCompatActivity {

    private TextInputLayout email_signIn, password_signIn;
    private Button signUp_signIn, signIn_signIn, forgotPassword_signIn;
    private ProgressBar progressBar_signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        email_signIn = findViewById(R.id.email_signIn);
        password_signIn = findViewById(R.id.password_signIn);

        progressBar_signIn = findViewById(R.id.progressBar_signIn);

        forgotPassword_signIn = findViewById(R.id.forgotPassword_signIn);
        forgotPassword_signIn.setOnClickListener(view -> forgotPassword());
        signIn_signIn = findViewById(R.id.signIn_signIn);
        signIn_signIn.setOnClickListener(view -> signIn());
        signUp_signIn = findViewById(R.id.signUp_signIn);
        signUp_signIn.setOnClickListener(view -> openSignUpActivity());
    }

    public void forgotPassword() {
        String email = email_signIn.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            email_signIn.setError("Enter your email!");
            email_signIn.getEditText().requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_signIn.setError("Enter a valid email!");
            email_signIn.getEditText().requestFocus();
            return;
        } else {
            email_signIn.setError("");
            email_signIn.setErrorEnabled(false);
        }

        progressBar_signIn.setVisibility(View.VISIBLE);
        FirebaseMethod.resetPassword(email,
                taskOnComplete -> {
                    if (taskOnComplete.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    }
                    progressBar_signIn.setVisibility(View.GONE);
                },
                taskOnFailure -> {
                    Toast.makeText(SignInActivity.this, "Please try again! Something went wrong.", Toast.LENGTH_LONG).show();
                    Log.e("FirebaseAuth Failure", taskOnFailure.toString());
                    if (taskOnFailure instanceof FirebaseAuthInvalidCredentialsException || taskOnFailure instanceof FirebaseAuthInvalidUserException) {
                        email_signIn.setError("Email does not exists!");
                        email_signIn.getEditText().requestFocus();
                    }
                });
    }

    public void signIn() {
        String email = email_signIn.getEditText().getText().toString().trim();
        String password = password_signIn.getEditText().getText().toString().trim();

        if (email.isEmpty()) {
            email_signIn.setError("Enter your email!");
            email_signIn.getEditText().requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_signIn.setError("Enter a valid email!");
            email_signIn.getEditText().requestFocus();
            return;
        } else {
            email_signIn.setError("");
            email_signIn.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            password_signIn.setError("Enter your password!");
            password_signIn.getEditText().requestFocus();
            return;
        } else {
            password_signIn.setError("");
            password_signIn.setErrorEnabled(false);
        }

        progressBar_signIn.setVisibility(View.VISIBLE);
        FirebaseMethod.signInWithEmailAndPassword(email, password,
                taskOnComplete -> {
                    if (taskOnComplete.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Sign In success", Toast.LENGTH_LONG).show();
                        FirebaseMethod.getUserDataFromRealtimeDatabase(FirebaseMethod.getCurrentUserUID(), new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    if (user.userType.equals("Parent")) {
                                        openParentDashboardActivity();
                                    } else {
                                        openChildDashboardActivity();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(SignInActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                                Log.e("DatabaseError", error.toString());
                            }
                        });
                    }
                    progressBar_signIn.setVisibility(View.GONE);
                },
                taskOnFailure -> {
                    Toast.makeText(SignInActivity.this, "Failed to sign in using your credentials", Toast.LENGTH_LONG).show();
                    Log.e("FirebaseAuthFailure", taskOnFailure.toString());
                });
    }

    public void openSignUpActivity() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }

    public void openParentDashboardActivity() {
        startActivity(new Intent(this, ParentDashboardActivity.class));
        finish();
    }

    public void openChildDashboardActivity() {
        startActivity(new Intent(this, ChildDashboardActivity.class));
        finish();
    }
}