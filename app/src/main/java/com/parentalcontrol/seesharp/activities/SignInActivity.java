package com.parentalcontrol.seesharp.activities;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.child.ChildDashboardActivity;
import com.parentalcontrol.seesharp.activities.child.PinLockscreenActivity;
import com.parentalcontrol.seesharp.activities.parent.ParentDashboardActivity;
import com.parentalcontrol.seesharp.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    private TextInputLayout email_signIn, password_signIn;
    //private Button signUp_signIn, signIn_signIn, forgotPassword_signIn;
    private ProgressBar progressBar_signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Button signUp_signIn, signIn_signIn, forgotPassword_signIn;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

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

        String email = Objects.requireNonNull(email_signIn.getEditText()).getText().toString().trim();

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
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Check your email to reset your password!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Please try again! Something went wrong.", Toast.LENGTH_LONG).show();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException || task.getException() instanceof FirebaseAuthInvalidUserException) {
                            email_signIn.setError("Email does not exists!");
                            email_signIn.getEditText().requestFocus();
                        }
                    }
                    progressBar_signIn.setVisibility(View.GONE);
                });
    }

    public void signIn() {
        String email = Objects.requireNonNull(email_signIn.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(password_signIn.getEditText()).getText().toString().trim();

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
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Sign In success", Toast.LENGTH_LONG).show();
                        openWhichActivity();
                    } else {
                        Toast.makeText(SignInActivity.this, "Failed to sign in using your credentials", Toast.LENGTH_LONG).show();
                        progressBar_signIn.setVisibility(View.GONE);
                    }
                });
    }

    public void openWhichActivity() {

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(SignInActivity.this, "You need to sign in first!", Toast.LENGTH_LONG).show();
            return;
        }

        firebaseDatabase.getReference("users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            if (user.userType.equals("Parent")) {
                                openParentDashboardActivity();
                            } else {
                                if (!user.pin.isEmpty()) {
                                    openLockScreenActivity(user.pin);
                                }
                                //openChildDashboardActivity();
                            }
                        } else {
                            Toast.makeText(SignInActivity.this, "Unable to retrieved user data!", Toast.LENGTH_LONG).show();
                        }
                        progressBar_signIn.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
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

    public void openLockScreenActivity(String pin) {
        Intent intent = new Intent(this, PinLockscreenActivity.class);
        intent.putExtra("pin", pin);
        startActivity(intent);
    }

    public void openChildDashboardActivity() {
        startActivity(new Intent(this, ChildDashboardActivity.class));
        finish();
    }
}