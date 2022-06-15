package com.parentalcontrol.seesharp.activities;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.parentalcontrol.seesharp.R;
import com.parentalcontrol.seesharp.activities.parent.ParentDashboardActivity;
import com.parentalcontrol.seesharp.model.User;
import com.parentalcontrol.seesharp.helper.DeviceHelper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    //private Button signIn_signUp, signUp_signUp;
    private TextInputLayout email_signUp, password_signUp, confirmPassword_signUp, fullName_signUp;
    private RadioGroup userType_signUp;
    private ProgressBar progressBar_signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button signIn_signUp, signUp_signUp;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        email_signUp = findViewById(R.id.email_signUp);
        password_signUp = findViewById(R.id.password_signUp);
        confirmPassword_signUp = findViewById(R.id.confirmPassword_signUp);
        fullName_signUp = findViewById(R.id.fullName_signUp);

        userType_signUp = findViewById(R.id.userType_signUp);

        progressBar_signUp = findViewById(R.id.progressBar_signUp);

        signIn_signUp = findViewById(R.id.signIn_signUp);
        signIn_signUp.setOnClickListener(view -> openSignInActivity());

        signUp_signUp = findViewById(R.id.signUp_signUp);
        signUp_signUp.setOnClickListener(view -> signUp());
    }

    private void signUp() {
        String email = Objects.requireNonNull(email_signUp.getEditText()).getText().toString().trim();
        String password = Objects.requireNonNull(password_signUp.getEditText()).getText().toString().trim();
        String confirmPassword = Objects.requireNonNull(confirmPassword_signUp.getEditText()).getText().toString().trim();
        String fullName = Objects.requireNonNull(fullName_signUp.getEditText()).getText().toString().trim();
        String userType = ((RadioButton) findViewById((userType_signUp).getCheckedRadioButtonId())).getText().toString().trim();

        if (email.isEmpty()) {
            email_signUp.setError("Enter your email!");
            email_signUp.getEditText().requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_signUp.setError("Enter a valid email!");
            email_signUp.getEditText().requestFocus();
            return;
        } else {
            email_signUp.setError("");
            email_signUp.setErrorEnabled(false);
        }

        if (password.isEmpty()) {
            password_signUp.setError("Enter your password!");
            password_signUp.getEditText().requestFocus();
            return;
        } else if (password.length() < 6) {
            password_signUp.setError("Minimum password length is 6!");
            password_signUp.getEditText().requestFocus();
            return;
        } else {
            password_signUp.setError("");
            password_signUp.setErrorEnabled(false);
        }

        if (confirmPassword.isEmpty()) {
            confirmPassword_signUp.setError("Re-enter your password!");
            confirmPassword_signUp.getEditText().requestFocus();
            return;
        } else if (!password.equals(confirmPassword)) {
            confirmPassword_signUp.setError("Mismatched password!");
            confirmPassword_signUp.getEditText().requestFocus();
            return;
        } else {
            confirmPassword_signUp.setError("");
            confirmPassword_signUp.setErrorEnabled(false);
        }

        if (fullName.isEmpty()) {
            fullName_signUp.setError("Enter your full name!");
            fullName_signUp.getEditText().requestFocus();
            return;
        } else {
            fullName_signUp.setError("");
            fullName_signUp.setErrorEnabled(false);
        }

        progressBar_signUp.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser != null) {
                            saveUserData(new User(firebaseUser.getUid(), email, password, fullName, userType, DeviceHelper.getDeviceName()));
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_LONG).show();
                            progressBar_signUp.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to register user", Toast.LENGTH_LONG).show();
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            email_signUp.setError("Email already exists!");
                            email_signUp.getEditText().requestFocus();
                        }
                        progressBar_signUp.setVisibility(View.GONE);
                    }

                });
    }

    private void saveUserData(User user) {
        firebaseDatabase.getReference("users")
                .child(user.accountId)
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                        if (user.userType.equals("Child")) {
                            showPin(user.pin);
                        } else {
                            openSignInActivity();
                        }

                    } else {
                        Toast.makeText(SignUpActivity.this, "Failed to save user data", Toast.LENGTH_LONG).show();
                    }
                    progressBar_signUp.setVisibility(View.GONE);
                });
    }

    private void openSignInActivity() {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }

        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void showPin(String pin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Copy and save pin");

        final TextView message = new TextView(this);
        message.setText(pin);
        builder.setView(message);

        // Set up the buttons
        builder.setPositiveButton("Copy and continue", (dialog, which) -> {
            String text = message.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getApplicationContext(), "Copied", Toast.LENGTH_LONG).show();
            openSignInActivity();
        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
        builder.show();
    }



}